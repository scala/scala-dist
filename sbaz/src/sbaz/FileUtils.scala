package sbaz
import java.io.{BufferedOutputStream, File, FileInputStream, FileOutputStream,
	FileReader, InputStream, OutputStream, Reader}

object FileUtils {

  /** Copies the contents from one file to another */
  def copyFile(from: File, to: File) {
    val in = new FileInputStream(from)
    val out = new FileOutputStream(to)
    pipeStream(in, out)
    in.close
    out.close
  }

  /** Copy the contents of a file to an OutputStream */
  def copyFile(file: File, out: OutputStream) {
    val in = new FileInputStream(file)
    pipeStream(in, out)
    in.close
  }


  /** Read the contents of the file */
  def readFile(file: File) = readReader(new FileReader(file))
  
  /** Read the contents of the named file */
  def readFile(filename: String): String = readReader(new FileReader(filename))

  private def readReader(in: Reader): String = {
    val buf = new Array[Char](1024)
    val sb = new StringBuilder
    var n: Int = 0
    
    def lp() {
      val numread = in.read(buf)
      if (numread >= 0) {
        sb.appendAll(buf, 0, numread)
        lp()
      }
    }
    lp()
    sb.toString
  }

  /** Pipe Bytes from an InputStream to an OutputStream */
  def pipeStream(in: InputStream, out: OutputStream) {
    val buf = new Array[Byte](1024)
    def lp() {
      val numread = in.read(buf)
      if (numread >= 0) {
        out.write(buf, 0, numread)
        lp()
      }
    }
    lp()
  }
  
  def isPack200(name: String) = name.endsWith(".pack")
  def isJar(name: String) = name.endsWith(".jar")
  
  /** Changes a name corresponding to a file by modifying the suffix */
  def rename(name: String, fromSuffix: String, toSuffix: String): String = {
    require(name.endsWith(fromSuffix))
    val baseName = name.substring(0, name.lastIndexOf(fromSuffix))
    baseName + toSuffix
  }

  /** Creates a new File with a modified suffix */
  def renameFile(file: File, fromSuffix: String, toSuffix: String): File = {
    val fromName = file.getName
    val toName = rename(fromName, fromSuffix, toSuffix)
    new File(file.getParent(), toName)
  }

  /** Unpacks the Pack200 in file to the out file. */
  def unpack200(in: File, out: File) {
    import java.util.jar.{Pack200, JarOutputStream}
    val unpacker = Pack200.newUnpacker()
    val jout = new JarOutputStream(new FileOutputStream(out))
    unpacker.unpack(in, jout)
    jout.close()
  }

  /** Applies pack200 to the given .jar file, generating a .pack file */
  def pack200(in: File): File = { 
    val out = renameFile(in, ".jar", ".pack")
    pack200(in, out)
    out
  }

  /** Applies the pack200 encoding on the in file to create the out file */
  def pack200(in: File, out: File) {
    import java.util.jar.{JarFile, Pack200}
    val packer = Pack200.newPacker
    val os = new BufferedOutputStream(new FileOutputStream(out))
    packer.pack(new JarFile(in), os)
    os.close
  }

  /** Generates the md5 checksum of the given file */
  def md5(file: File): String = {
    import java.math.BigInteger
    import java.security.MessageDigest
    val digester = MessageDigest.getInstance("MD5");
    val in = new FileInputStream(file)
    val buf = new Array[Byte](1024)
    def lp() {
      val numread = in.read(buf)
      if (numread >= 0) {
        digester.update(buf, 0, numread)
        lp() 
      }
    }
    lp()
    in.close
    val md5 = new BigInteger(1,digester.digest()).toString(16);
    "0" * (32-md5.length) + md5
  }
}
