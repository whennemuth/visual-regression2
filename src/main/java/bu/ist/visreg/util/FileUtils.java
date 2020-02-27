package bu.ist.visreg.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileUtils {

	private File f;
	public FileUtils() {
		super();
	}
	public FileUtils(File f) {
		setFile(f);
	}
	public FileUtils(String pathname) {
		setFile(pathname);
	}
	public void setFile(File f) {
		this.f = f;
	}
	public void setFile(String pathname) {
		this.f = new File(pathname);
	}	
	public File getFile() {
		return f;
	}
	
	public String readFile() {
		if(f.isFile()) {
			try {
				byte[] bytes = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
				return bytes.toString();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public boolean writeFile(String s) {
		Path result;
		StandardOpenOption opt = f.exists() ? 
			StandardOpenOption.TRUNCATE_EXISTING : 
			StandardOpenOption.CREATE_NEW;		
		try {
			result = Files.write(Paths.get(f.getAbsolutePath()), s.getBytes(), opt);
		} 
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return result.toFile().isFile();
	}
	
	public FileUtils newInstance(File f) {
		return new FileUtils(f);
	}

	public boolean delete() {
		return f.delete();
	}
	public boolean isFile() {
		return f.isFile();
	}
	public String getName() {
		return f.getName();
	}
	public boolean renameTo(File targetFile) {
		return f.renameTo(targetFile);
	}
	public String getAbsolutePath() {
		return f.getAbsolutePath();
	}
	public File getParentFile() {
		return f.getParentFile();
	}
	public boolean isDirectory() {
		return f.isDirectory();
	}
	public boolean mkdirs() {
		return f.mkdirs();
	}
	public File[] listFiles() {
		return f.listFiles();
	}
}
