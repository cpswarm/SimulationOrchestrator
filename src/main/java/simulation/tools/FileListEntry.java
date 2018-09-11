package simulation.tools;

public class FileListEntry {
	String zipEntry;
	String filePath;
	
	public FileListEntry(final String zipEntry, final String filePath) {
		this.zipEntry = zipEntry;
		this.filePath = filePath;
	}

	public String getZipEntry() {
		return zipEntry;
	}

	public String getFilePath() {
		return filePath;
	}
}
