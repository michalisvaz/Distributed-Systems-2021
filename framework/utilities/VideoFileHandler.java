//package utilities;
//
//import java.util.List;
//import java.util.Map;
//
//public class VideoFileHandler {
//
//    public static final int CHUNK_SIZE = 512 * 1024;
//
//    /**
//     * Read info about the mp4 files corresponding to a certain channel and/or certain hashtags
//     * TODO: Implement it. (I think) we shouldn't store all the videos in RAM when doing this.
//     * TODO: We must decide on the format of the videos' names. For example "Dog catches ball; Hashtags(dog, ball); Channel(interfacerz)"
//     *
//     * @param directory where the videos are stored
//     * @param channel  only videos belonging to this channel should be read
//     * @param hashtags only videos belonging to one or more hashtags of these should be read
//     * @return a map where each filename is mapped to a VideoFile object
//     */
//    public static Map<String, VideoFile> readInfo(String directory, String channel, List<String> hashtags) {
//
//    }
//
//    /**
//     * Reads specified mp4 file
//     * TODO: Implement it
//     *
//     * @param name the name of the file to be read
//     * @return a VideoFile object representing the file read
//     */
//    public static VideoFile readFile(String name) {
//
//    }
//
//    /**
//     * Writes the VideoFile objects to directory folderName in separate mp4 files
//     * TODO: Implement it. Maybe not needed in the first phase
//     *
//     * @param chunkList the files to be written
//     * @param folderName the subdirectory of the working directory were the files will be written
//     * @return true if everything went well, false if there were problems
//     */
//    public static boolean writeFiles(List<VideoFile> chunkList, String folderName){
//
//    }
//
//    /**
//     * Writes the VideoFile object to directory folderName in a mp3 file
//     * TODO: Implement it
//     *
//     * @param video the file to write
//     * @param folderName the subdirectory of the working directory were the file will be written
//     * @return true if everything went well, false if there were problems
//     */
//    public static boolean writeFile(VideoFile video, String folderName){
//
//    }
//
//    /**
//     * Splits a video to many chunks
//     * TODO: Implement it
//     *
//     * @param video the video to be split
//     * @return a List of VideoFile objects with the chunks of the original video
//     */
//    public static List<VideoFile> split(VideoFile video){
//
//    }
//
//    /**
//     * Merge many small videos (chunks) into one (larger) video
//     * TODO: Implement it
//     *
//     * @param chunkList the list of the small videos to be merged
//     * @return the VideoFile which is the result of the merging
//     */
//    public static VideoFile merge(List<VideoFile> chunkList){
//
//    }
//
//}
