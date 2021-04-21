import utilities.Utilities;
import utilities.VideoFile;
import utilities.VideoFileHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class TestHandler {
	
	public static void main(String args[]){
		
		// ---------------- test readInfo ----------------
		String directory1 = "C:\\Users\\michv\\Desktop\\Staff\\Notes+\\Current Semester\\01-Distributed Systems\\Project\\framework\\Videos";
		HashMap<String, VideoFile> map = VideoFileHandler.readInfo(directory1, "any", "any");
		Utilities.print(map);
		
		// ---------------- test the rest ----------------
		VideoFile testVideoFile = VideoFileHandler.readFile(directory1, "Best number; #bbt #awesome; channel1.mp4");
		ArrayList<VideoFile> testVideoFileList = VideoFileHandler.split(testVideoFile);
		String directory2 = "C:\\Users\\michv\\Desktop\\Staff\\Notes+\\Current Semester\\01-Distributed Systems\\Project\\framework\\result_split";
		VideoFileHandler.writeFiles(testVideoFileList, directory2);
		VideoFile result = VideoFileHandler.merge(testVideoFileList);
		String directory3 = "C:\\Users\\michv\\Desktop\\Staff\\Notes+\\Current Semester\\01-Distributed Systems\\Project\\framework\\result_merge";
		VideoFileHandler.writeFile(result, directory3);
	}
	
	
	
}
