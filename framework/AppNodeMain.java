// import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class AppNodeMain {


    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        while (true) {
            int input = menu();
            switch (input) {
            case 1:
            case 2:
                System.out.println("Video name: ");
                String videoName = sc.nextLine();
                // showNameVids(videoName);
            case 3:
                System.out.println("Creator's name: ");
                String creator = sc.nextLine();
                // showCreatorVids(creator);
            case 4:
                System.out.println("Hashtag: ");
                String hashtag = sc.nextLine();
                // showHashtagVids(hashtag);
            default:
                System.exit(0);

            }
        }

        // System.out.println(menu());
    }

    private static int menu() {
        System.out.println("---------- MENU ----------");
        System.out.println("1\tUpload video");
        System.out.println("2\tSearch video name");
        System.out.println("3\tSearch creator");
        System.out.println("4\tSearch hashtag");
        System.out.println("0\tExit App");
        System.out.println("--------------------------");

        System.out.print("Enter: ");
        String userInput = sc.nextLine();
        sc.close();
        return userInput != null ? Integer.parseInt(userInput) : 0;
    }

}