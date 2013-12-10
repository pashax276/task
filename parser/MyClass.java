import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MyClass {
    public static void main(String[] args) throws IOException, InterruptedException {
        String YANDEX = "market.yandex.ua";
        String HOTLINE = "hotline.ua";
        String PRICE = "price.ua";

        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("1 - " + YANDEX);
        System.out.println("2 - " + HOTLINE);
        System.out.println("3 - " + PRICE);

        String consoleRead = console.readLine();

        int chose = Integer.parseInt(consoleRead);

        switch (chose) {
            case 1:
                YandexMarket yandexMarket = new YandexMarket();
                yandexMarket.start();

                break;
            case 2:

                break;
            case 3:

                break;
        }
    }
}
