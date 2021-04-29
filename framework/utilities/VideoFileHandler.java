package utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class VideoFileHandler {

	public static final int CHUNK_SIZE = 256 * 1024;

	/**
	 * Read info about the mp4 files corresponding to a certain channel and/or
	 * certain hashtags Name format for videos: "Dog catches ball; #dog #ball;
	 * channel1"
	 *
	 * @param directory where the videos are stored
	 * @param channel   only videos belonging to this channel should be read
	 * @param hashtag   only videos with this hashtag should be read
	 * @return a map where each filename is mapped to a VideoFile object
	 */
	public static HashMap<String, VideoFile> readInfo(String directory, String channel, String hashtag) {
		File dir = new File(directory);
		if (!dir.isDirectory()) {
			return null;
		}
		HashMap<String, VideoFile> map = new HashMap<String, VideoFile>();
		File[] directoryListing = dir.listFiles();
		for (File f : directoryListing) {
			String fullNameWithSuffix = f.getName();
			if (f.isDirectory()) {
				Utilities.print("Skipped as folder: " + fullNameWithSuffix);
			}
			String fullName = fullNameWithSuffix.substring(0, fullNameWithSuffix.lastIndexOf('.'));
			String[] parts = fullName.split(";");
			if (parts.length != 3) {
				Utilities.print("Wrong name format: " + fullName);
				continue;
			}
			String name = parts[0];
			ArrayList<String> hts = new ArrayList<String>();
			for (String ht : parts[1].split("#")) {
				String tmp = ht.trim().toLowerCase();
				if (tmp.length() > 0) {
					hts.add(tmp);
				}
			}
			String chanel = parts[2].trim().toLowerCase();
			long size = f.length();
			boolean flag_channel = chanel.equalsIgnoreCase(channel) || channel.equals("any");
			boolean flag_hashtag = hts.contains(hashtag.trim().toLowerCase().replace("#", "")) || hashtag.equals("any");
			if (flag_channel && flag_hashtag) {
				map.put(fullNameWithSuffix, new VideoFile(name, chanel, hts, size));
			}
		}
		if (map.isEmpty()) {
			Utilities.printError("No files matching your criteria found");
		}
		return map;
	}

	/**
	 * Reads specified mp4 file
	 *
	 * @param name the name of the file to be read
	 * @return a VideoFile object representing the file read
	 */
	public static VideoFile readFile(String directory, String name) {
		File file = new File(directory + "/" + name);
		if (!file.exists()) {
			Utilities.printError("File not Found");
			return null;
		} else if (file.isDirectory()) {
			Utilities.printError("Directory instead of file");
			return null;
		}
		String[] parts = name.split(";");
		if (parts.length != 3) {
			Utilities.printError("Wrong name for file to read");
			return null;
		}
		String videoName = parts[0];
		ArrayList<String> hts = new ArrayList<String>();
		for (String ht : parts[1].split("#")) {
			hts.add(ht.trim().toLowerCase());
		}
		String chanel = parts[2];
		long size = file.length();
		VideoFile videoFile = new VideoFile(videoName, chanel, hts, size);
		try {
			byte[] fileContent = Files.readAllBytes(file.toPath());
			videoFile.setData(fileContent);
			return videoFile;
		} catch (IOException e) {
			Utilities.printError("Something went wrong while reading file" + name);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Writes the VideoFile objects to directory folderName in separate mp4 files
	 *
	 * @param chunkList  the files to be written
	 * @param folderName the subdirectory of the working directory were the files
	 *                   will be written
	 * @return true if everything went well, false if there were problems
	 */
	public static boolean writeFiles(List<VideoFile> chunkList, String folderName) {
		if (chunkList.isEmpty()) {
			return false;
		}
		for (VideoFile vf : chunkList) {
			boolean flag = writeFile(vf, folderName);
			if (!flag) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Writes the VideoFile object to directory folderName in a mp3 file
	 *
	 * @param video      the file to write
	 * @param folderName the subdirectory of the working directory were the file
	 *                   will be written
	 * @return true if everything went well, false if there were problems
	 */
	public static boolean writeFile(VideoFile video, String folderName) {
		if (video == null) {
			return false;
		}
		File theDir = new File(folderName);
		if (!theDir.exists()) {
			theDir.mkdirs();
		}
		String fullName = folderName + "/" + video.getName() + ".mp4";
		try (FileOutputStream fos = new FileOutputStream(fullName)) {
			fos.write(video.getData());
			return true;
		} catch (IOException e) {
			Utilities.printError("Something went wrong while writing file" + fullName);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Splits a video to many chunks
	 *
	 * @param video the video to be split
	 * @return a List of VideoFile objects with the chunks of the original video
	 */
	public static ArrayList<VideoFile> split(VideoFile video) {
		int n = (int) Math.ceil((double) video.getSize() / (double) CHUNK_SIZE);
		ArrayList<VideoFile> toReturn = new ArrayList<VideoFile>();
		if (n == 0) {
			return toReturn;
		}
		for (int i = 0; i < n - 1; i++) {
			String name = String.format("%03d", i) + video.getName();
			VideoFile temp = new VideoFile(name, video.getChannel(), video.getHashtags(), CHUNK_SIZE);
			temp.setData(Arrays.copyOfRange(video.getData(), i * CHUNK_SIZE, (i + 1) * CHUNK_SIZE));
			toReturn.add(temp);
		}
		String name = String.format("%03d", n - 1) + video.getName();
		VideoFile temp = new VideoFile(name, video.getChannel(), video.getHashtags(), video.getSize() % CHUNK_SIZE);
		temp.setData(Arrays.copyOfRange(video.getData(), (n - 1) * CHUNK_SIZE, (int) (video.getSize())));
		toReturn.add(temp);
		return toReturn;
	}

	/**
	 * Merge many small videos (chunks) into one (larger) video
	 *
	 * @param chunkList the list of the small videos to be merged
	 * @return the VideoFile which is the result of the merging
	 */
	public static VideoFile merge(List<VideoFile> chunkList) {
		if (chunkList.isEmpty()) {
			return null;
		}
		int chunks = chunkList.size();
		byte[] resData = new byte[(int) ((chunks - 1) * CHUNK_SIZE + chunkList.get(chunks - 1).getSize())];
		int cnt = 0;
		for (VideoFile vf : chunkList) {
			byte[] temp = vf.getData();
			System.arraycopy(temp, 0, resData, cnt, (int) (vf.getSize()));
			cnt += vf.getSize();
		}
		VideoFile f = chunkList.get(0);
		VideoFile res = new VideoFile(f.getName().substring(3), f.getChannel(), f.getHashtags(), f.getSize());
		res.setData(resData);
		return res;
	}

}
