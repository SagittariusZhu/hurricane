package org.iipg.hurricane.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class FileUtil {

    public static Properties readPropertiesFile(String filename)  
    {  
        Properties properties = new Properties();  
        try  {
        	ClassLoader loader = FileUtil.class.getClassLoader();
        	
            InputStream inputStream = loader.getResourceAsStream(filename);
            properties.load(inputStream);  
            inputStream.close(); //关闭流  
        }  
        catch (IOException e)  
        {  
            e.printStackTrace();  
        }  
        return properties; 
    }  
    
	public final static synchronized long writeXmlFile( File file, Document document )
	throws Exception
	{
		DOMSource doms = new DOMSource( document );
		StreamResult result = new StreamResult( file );
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		Properties properties = transformer.getOutputProperties();
		properties.setProperty( OutputKeys.ENCODING, "utf-8" );
		properties.setProperty( OutputKeys.METHOD, "xml" );
		properties.setProperty( OutputKeys.INDENT, "yes" );
		transformer.setOutputProperties( properties );
		transformer.transform( doms, result );
		return 0;
	}
	
	public static boolean cloneFile(String src, String dest) throws IOException {
        InputStream in = new FileInputStream(src);  
        OutputStream out = new FileOutputStream(dest);  
        byte[] buffer = new byte[1024];  
        int read = 0;  
        while((read = in.read(buffer))!= -1){  
            out.write(buffer,0,read);  
        }  
        in.close();  
        out.close(); 
        return true;
	}

	public static boolean clone(String src, String dest) throws IOException {
		File srcFile = new File(src);
		File destFile = new File(dest);
		if (srcFile.isFile()) {
			return cloneFile(src, dest);
		}
		
		if (destFile.exists()) {
			throw new IOException("Dest file exist!");
		}
		
		destFile.mkdirs();
		
		File[] file= srcFile.listFiles();  
		for(int i=0;i<file.length;i++){  
			clone(file[i].getAbsolutePath(), dest + File.separator + file[i].getName());				
		}  
		return true;
	}

	public static void removeFile(File file) {
		if (file.exists()) {
			file.delete();
		}		
	}

	public static byte[] read(File f) {
		if (f.exists() && f.isFile()) {
			try {
				FileInputStream fis = new FileInputStream(f);
				FileChannel fc = fis.getChannel();
				byte[] ret = (byte[]) Array.newInstance(byte.class, (int) fc.size());
				fis.read(ret);
				return ret;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NegativeArraySizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		}
		return null;
	}
	
	public static Manifest findManifest(final Class<?> clazz) throws IOException,
	URISyntaxException{
		Class cls = clazz;
		URL codeLocation = cls.getProtectionDomain().getCodeSource().getLocation();
		if (codeLocation == null) {
			URL r = cls.getResource("");
			synchronized (r) {
				String s = r.toString();
				Pattern jar_re = Pattern.compile("jar:\\s?(.*)!/.*");
				Matcher m = jar_re.matcher(s);
				if (m.find()) { // the code is run from a jar file.
					s = m.group(1);
				} else {
					String p = cls.getPackage().getName().replace('.', '/');
					s = s.substring(0, s.lastIndexOf(p));
				}
				try {
					codeLocation = new URL(s);
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		System.out.println("codeLocation: " + codeLocation);
		final JarFile jf = new JarFile(new File(codeLocation.toURI()));

		//final JarFile jf = new JarFile(new File("C:\\workspace\\hmw\\java\\trunk\\dist\\hu-server-1.0.0-SNAPSHOT.jar"));
		//final ZipEntry entry = jf.getEntry("META-INF/MANIFEST.MF");
		return jf.getManifest();
	}
}
