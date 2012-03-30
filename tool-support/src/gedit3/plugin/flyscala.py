# -*- coding: utf-8 -*-
#
#  Scala compilation on the fly.
#
#  Automatically calls the fast scala compiler and gives on-the-fly
#  compilation feedback.
#
#  Copyright (C) 2012 Sarah Mount <s.mount@wlv.ac.uk>
#   
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#   
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#   
#  You should have received a copy of the GNU General Public License
#  along with this program; if not, write to the Free Software
#  Foundation, Inc., 59 Temple Place, Suite 330,
#  Boston, MA 02111-1307, USA.


from gi.repository import GObject
from gi.repository import Gtk
from gi.repository import Gedit
from gi.repository import Pango

import os
import re
import subprocess


UI_XML = """
<ui>
  <menubar name="MenuBar">
    <placeholder name="ExtraMenu_1">
      <menu name="ScalaMenu" action="Scala">
          <menuitem name="RunScalaAction" action="RunScalaAction"/>
          <menuitem name="CompileALlScalaAction" action="CompileAllScalaAction"/>
          <menuitem name="ResetFSCAction" action="ResetFSCAction"/>
      </menu>
    </placeholder>
  </menubar>
</ui>
"""

        
class FlyScalaPlugin(GObject.Object, Gedit.WindowActivatable):
    __gtype_name__ = 'FlyScala'
    window = GObject.property(type=Gedit.Window)
   
    def __init__(self):
        GObject.Object.__init__(self)
        self._fsc = None
        self._actions = None
        self._ui_merge_id = None
        return

    def _add_ui(self):
        manager = self.window.get_ui_manager()
        self._actions = Gtk.ActionGroup('ScalaActions')
        self._actions.add_actions([
            ('Scala', None, "S_cala", None, None, None),
            ('RunScalaAction', Gtk.STOCK_EXECUTE,
             'Compile and run this file...', 'F5',
             'Run the current Scala document', 
                self.on_run_scala_action_activate),
            ('CompileAllScalaAction', Gtk.STOCK_EXECUTE,
             '_Compile this directory...', 'F6',
             'Compile all Scala files in the current directory', 
                self.on_compile_all_scala_action_activate),
            ('ResetFSCAction', Gtk.STOCK_REFRESH, '_Restart fsc', 
                None, 'Restart the fast Scala compiler', 
                self.on_reset_fsc_action_activate),
        ])
        manager.insert_action_group(self._actions)
        self._ui_merge_id = manager.add_ui_from_string(UI_XML)
        manager.ensure_update()
        self.window.set_data('FlyScalaHandlers', [])
        self._fsc = FastScalaCompiler(self, self.window)
        self._fsc.add_ui()
        return

    def _add_handler(self, handler_id):
        handlers = self.window.get_data('FlyScalaHandlers')
        handlers.append(handler_id)
        self.window.set_data('FlyScalaHandlers', handlers)
        return
    
    def on_tab_added(self, window, tab, data=None):
        # pylint: disable-msg=W0613
        doc = tab.get_document()
        handler_id1 = doc.connect('saved', self.on_document_saved)
        self._add_handler(handler_id1)
        handler_id2 = doc.connect('loaded', self.on_document_loaded)
        self._add_handler(handler_id2)        
        return

    def on_document_loaded(self, document, data=None):
        # pylint: disable-msg=W0613
        self._fsc.compile()
        return
    
    def on_document_saved(self, document, data=None):
        # pylint: disable-msg=W0613
        self._fsc.compile()
        return
                            
    def do_activate(self):
        self._add_ui()
        handler_id = self.window.connect('tab-added', self.on_tab_added)
        self._add_handler(handler_id)
        return

    def do_deactivate(self):
        handlers = self.window.get_data('FlyScalaHandlers')
        for handler_id in handlers:
            self.window.disconnect(handler_id)
        self._remove_ui()
        return

    def do_update_state(self):
        pass

    def on_compile_all_scala_action_activate(self, action, data=None):
        # pylint: disable-msg=W0613
        self._fsc.compile(folder=True)
        return
    
    def on_run_scala_action_activate(self, action, data=None):
        # pylint: disable-msg=W0613
        self._fsc.run()
        return

    def on_reset_fsc_action_activate(self, action, data=None):
        # pylint: disable-msg=W0613
        self._fsc.reset()
        return
    
    def _remove_ui(self):
        manager = self.window.get_ui_manager()
        manager.remove_ui(self._ui_merge_id)
        manager.remove_action_group(self._actions)
        manager.ensure_update()
        self._fsc.remove_ui()
        return


class ScalaCompilerMessage(object):

    ERRTYPES = dict(W='warning', E='error')
    
    def __init__(self, fname, lineno, errtype, msg, code, carat):
        self.file = fname
        self.lineno = int(lineno)
        self.errtype = errtype
        self.msg = msg
        self.code = code.strip()
        self.carat = carat
        return

    @staticmethod
    def factory(errortext):
        """Parse Scala compiler error text and return a list of instances
        of ScalaCompilerMessage.
        """
        # pylint: disable-msg=W0141
        # pylint: disable-msg=W0142
        msg_re = re.compile(r'^(?P<file>[^:]+):'
                            r'(?P<lineno>\d+): '
                            r'(?P<type>[A-Za-z]+): '
                            r'(?P<msg>.*)\n'
                            r'(?P<code>.*)\n'
                            r'(?P<carat>\s*\^\s*)\n',
                            flags=re.MULTILINE)
        matches = msg_re.findall(errortext)
        return map(lambda m: ScalaCompilerMessage(*m), matches)

    def __str__(self):
        # pylint: disable-msg=W0141
        msg = [self.file, ':', self.lineno, ': ', self.errtype,
               ': ', self.msg, '\n', self.code, '\n', self.carat, '\n']
        return ''.join(map(str, msg))

        
class FastScalaCompiler(Gtk.HBox):
    """A widget to display the output of running Scala programs.
    Implements compiling and running Scala programs, along with
    displaying the output of fsc and scala.

    Largely based on Quixotix Django plugin for Gedit:
    https://github.com/Quixotix/gedit-django-project
    """

    __gtype_name__ = "ScalaOutputPanel"
    
    def __init__ (self, plugin, window):
        Gtk.HBox.__init__(self, homogeneous=False, spacing=4) 
        self._bottom_widget = None
        self._plugin = plugin
        self._window = window
        scrolled = Gtk.ScrolledWindow()
        self._view, self._liststore = self._create_view()
        scrolled.add(self._view)
        self.pack_start(scrolled, True, True, 0)
        self.set_font("monospace 10")
        self.show_all()
        return
    
    def is_scala(self):
        """Is the current document a Scala program?
        """
        doc = self._window.get_active_document()
        try:
            lang = doc.get_language().get_name()
        except AttributeError:
            return False
        return lang.lower() == 'scala'
    
    def reset(self):
        """Reset fsc when something has gone wrong.
        """
        process = subprocess.Popen(['fsc', '-reset'],
                                   stdout=open('/dev/null', 'w'),
                                   stderr=open('/dev/null', 'w'),
                                   cwd='/tmp/')
        process.wait ()
        return

    def _run(self, cmd='fsc', ext=True, folder=False):
        """Run some command on the current document and return output.
        Do nothing if the current document is not a Scala program.
        If ext is False, strip the trailing .scala from the filename.
        If folder is True, use '*.scala' rather than a filename.
        """
        # pylint: disable-msg=W0141
        doc = self._window.get_active_document()
        # Only run fsc if the current document is Scala code.
        if not self.is_scala():
            self._status('%s is not a Scala file.' %
                         doc.get_uri_for_display())
            return None, None
        # Get the path and filename of the current document.
        location = doc.get_location()
        path = os.sep.join(location.get_path().split(os.sep)[:-1])        
        if folder:
            files = os.listdir(path)
            cmdline = filter(lambda s: s.endswith('.scala'), files)
            cmdline.insert(0, cmd)
        elif not ext:
            basename = location.get_basename()
            basename = basename.split('.')[0]
            cmdline = [cmd, basename]
        else:
            basename = location.get_basename()
            cmdline = [cmd, basename]
        # Run compiler or runtime tool and capture output.
        self._status(' '.join(cmdline))
        process = subprocess.Popen(cmdline,
                                   stdout=subprocess.PIPE,
                                   stderr=subprocess.PIPE,
                                   cwd=path)
        process.wait()
        output = process.communicate()
        return output, process.returncode
    
    def run(self):
        """Run Scala code.
        Assume the current document has already been compiled.
        Assume the object name is the same as the un-suffixed document name.
        """
        self.compile()
        output, returncode = self._run(cmd='scala', ext=False)
        if returncode is None: # No Scala document.
            return
        self._display_tool_output(returncode, output, tool='Scala')
        return
    
    def compile(self, folder=False):
        """Compile the current document.
        """
        output, returncode = self._run(folder=folder)
        if returncode is None:         # No Scala document.
            return None, None
        if returncode == 0:            # No errors to process.
            self._remove_tags()        # Remove old tags.
            self._clear()              # Clear output pane.
            return None, returncode
        text = output[0] if output[0] else output[1]
        messages = ScalaCompilerMessage.factory(text)
        self._highlight_errors(messages)
        if returncode is None: # No Scala document.
            return
        self._display_tool_output(returncode, output, tool='Compiler')
        return

    def _remove_tags(self, doc=None):
        if doc is None:
            doc = self._window.get_active_document()
        start, end = doc.get_bounds()
        doc.remove_tag_by_name('flyscala-error', start, end)
        doc.remove_tag_by_name('flyscala-warning', start, end)
        return

    def _highlight_errors(self, messages):
        """Add tags to all lines of code with warnings or errors.
        """
        self._create_tags()
        flag = Gtk.TextSearchFlags.TEXT_ONLY
        docs = {}
        for doc in self._window.get_documents():
            # fsc may give errors with full paths or just basenames.
            # So this dict should have both.
            docs[doc.get_uri_for_display()] = doc
            docs[os.path.basename(doc.get_uri_for_display())] = doc
            self._remove_tags(doc)
        for message in messages:
            # Which document is the error in?
            if message.file in docs:
                doc = docs[message.file]
            else: # Document is not open.
                return 
            # Where is the error?
            start = doc.get_iter_at_line(message.lineno - 1)
            end = doc.get_iter_at_line(message.lineno)
            match = start.forward_search(message.code, flag, end)
            doc.apply_tag_by_name('flyscala-' + message.errtype,
                                  match[0], match[1])
        return

    def on_error_clicked(self, selection):
        doc = self._window.get_active_document()
        model, treeiter = selection.get_selected()
        if treeiter is not None:
            errors = ScalaCompilerMessage.factory(model[treeiter][0])
            if errors == []: return
            location = doc.get_uri_for_display()
            if (location == errors[0].file or
                os.path.basename(location) == errors[0].file):
                doc.goto_line(errors[0].lineno - 1)
                docview = self._window.get_active_view()
                docview.scroll_to_cursor()
        return
    
    def _create_view(self):
        """Create the gtk.TextView used for compiler / runtime output.
        """
        model = Gtk.ListStore(str)
        view = Gtk.TreeView(model=model)
        err_col = Gtk.TreeViewColumn('fsc and scala output',
                                     cell_renderer=Gtk.CellRendererText(),
                                     text=0)
        view.set_headers_visible(False)
        view.append_column(err_col)
        select = view.get_selection()
        select.connect('changed', self.on_error_clicked)
        return view, model
    
    def _display_tool_output(self, returncode, output, tool='Compiler'):
        """Display the output of a compiler or runtime to the output pane.
        """
        if returncode == 0:
            self._insert(['%s finished successfully.\n' % tool], style=None)
            if output is None:
                return
        text = output[0] if output[0] else output[1]
        messages = ScalaCompilerMessage.factory(text)
        if messages == []:
            self._insert([text, 'Exit: %s\n\n' % returncode], style='dark grey')
        else:
            self._insert(messages, style='red')
        return
    
    def _clear(self):
        """Clear the output panel.
        """
        self._liststore.clear()
        return

    def _insert(self, messages, style=None, append=False):
        """Insert text, apply styling.
        """
        # pylint: disable-msg=W0141
        if not append:
            self._clear()
        for msg in messages:
            self._liststore.append([str(msg)])
        cells = self._view.get_column(0).get_cells()
        if style is None:
            style = 'black'
        for cell in cells:
            cell.set_property('foreground', style)
            cell.set_property('weight', 500)
        return

    def _append(self, messages, style=None):
        """Append text to the output pane.
        """
        self._insert(messages, style, True)
        return

    def _create_tags(self, doc=None):
        """Create error and warning tags that will annotate source code.
        """
        if doc is None:
            doc = self._window.get_active_document()

        err = doc.get_tag_table().lookup('flyscala-error')
        if err is None:
            doc.create_tag(tag_name = 'flyscala-error',
                           underline = Pango.Underline.ERROR)
        warn = doc.get_tag_table().lookup('flyscala-warning')
        if warn is None:
            doc.create_tag(tag_name = 'flyscala-warning',
                           underline = Pango.Underline.SINGLE)
        return
    
    def add_ui(self):
        # Add to bottom panel of Gedit
        panel = self._window.get_bottom_panel()
        panel.add_item_with_stock_icon(self, "ScalaOutput",
                                       "Scala Output", Gtk.STOCK_EXECUTE)
        return
    
    def remove_ui(self):
        if self._view:
            panel = self._window.get_bottom_panel()
            panel.remove_item(self)
            return

    def _status(self, msg):
        """Show a message in the status bar, if possible.
        """
        statusbar = self._window.get_statusbar()
        context_id = statusbar.get_context_id('FlyScala')
        statusbar.push(context_id, 'flyscala: ' + msg)
        return

    def set_font(self, font_name):
        font_desc = Pango.FontDescription(font_name)
        self._view.modify_font(font_desc)
        return
