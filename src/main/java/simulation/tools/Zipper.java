package simulation.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zipper
{
	List<FileListEntry> fileList;
	private String sourceFolder = "C:\\testzip";

	public Zipper(String source){
		this.sourceFolder = source;
		fileList = new ArrayList<FileListEntry>();
	}

	public void updateSourceFolder(final String sourceFolder) {
		this.sourceFolder = sourceFolder;
	}
	
	/**
	 * Zip it
	 * @param zipFile output ZIP file location
	 */
	public void zipIt(String zipFile){

		byte[] buffer = new byte[1024];

		try{

			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);

			System.out.println("Output to Zip : " + zipFile);

			for(FileListEntry file : this.fileList){

				System.out.println("File Added : " + file.getZipEntry());
				ZipEntry ze = new ZipEntry(file.getZipEntry());
				zos.putNextEntry(ze);

				FileInputStream in =
						new FileInputStream(file.getFilePath());

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}

				in.close();
			}

			zos.closeEntry();
			//remember close it
			zos.close();

			System.out.println("Done");
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}

	/**
	 * Traverse a directory and get all files,
	 * and add the file into fileList
	 * @param node file or directory
	 * 
	 * @return String
	 * 		path of the file to be sent to the Optimization Tool
	 */
	public String generateFileList(File node){
		String result = "";
		//add file only
		if(node.isFile()){
			// It excludes from the file list the file zse, which it has to be send to the Optimization Tool
			if(!node.getAbsoluteFile().toString().endsWith("zse")) {
				fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
			} else {
				result = node.getAbsoluteFile().toString();
			}
		}

		if(node.isDirectory()){
			String[] subNote = node.list();
			for(String filename : subNote){
				String res = generateFileList(new File(node, filename));
				if (!res.isEmpty()) {
					result = res;
				}
			}
		}
		return result;
	}

	/**
	 * Format the file path for zip
	 * @param file file path
	 * @return Formatted file path
	 */
	private FileListEntry generateZipEntry(String file){
		FileListEntry entry = new FileListEntry(file.substring(sourceFolder.length(), file.length()), file);
		return entry;
	}
}