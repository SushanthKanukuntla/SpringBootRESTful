package com.finra.objects;

public class FileMetaData {
	
	private String lastAccessTime;
	
	private String isDirectory;
	
	private String isOther;
	
	private String isRegularFile;
	
	private String isSymbolicLink;
	
	private String size;

	public String getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(String lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public String getIsDirectory() {
		return isDirectory;
	}

	public void setIsDirectory(String isDirectory) {
		this.isDirectory = isDirectory;
	}

	public String getIsOther() {
		return isOther;
	}

	public void setIsOther(String isOther) {
		this.isOther = isOther;
	}

	public String getIsRegularFile() {
		return isRegularFile;
	}

	public void setIsRegularFile(String isRegularFile) {
		this.isRegularFile = isRegularFile;
	}

	public String getIsSymbolicLink() {
		return isSymbolicLink;
	}

	public void setIsSymbolicLink(String isSymbolicLink) {
		this.isSymbolicLink = isSymbolicLink;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
	
}
