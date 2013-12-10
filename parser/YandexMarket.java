import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class YandexMarket {

    public void start() throws IOException, InterruptedException {

        SearchParam searchParam = new SearchParam();

        ArrayList listNum = new ArrayList();
        ArrayList listShop = new ArrayList();
        ArrayList listLinkShop = new ArrayList();
        ArrayList listNameProd = new ArrayList();
        ArrayList<Integer> pageNumber = new ArrayList<Integer>();

        System.out.println("Вы выбрали market.yandex.ua");
        System.out.print("Что ищем: ");
        BufferedReader searchBuf = new BufferedReader(new InputStreamReader(System.in));

        String searchLine = searchBuf.readLine();

        pageNumber.add(0, 1);

        System.out.println("Количество страниц: " + pageNumber);

        int pages = 1;

        for (int i = 1; i <= pages; i++) {

            Thread.sleep(100);

            @Deprecated
            URL url = new URL("http://www.market.yandex.ua/search.xml?text=" + URLEncoder.encode(searchLine) + "&cvredirect=2" + "page%3D2&" + "page=" + i);
            System.out.println(url);

            StringBuilder sb = new StringBuilder();
            StringBuilder priceBuild = new StringBuilder();
            StringBuilder shopBuild = new StringBuilder();
            StringBuilder linkShopBuild = new StringBuilder();
            StringBuilder nameProdBuild = new StringBuilder();

            StringBuilder buildPage = new StringBuilder();
            StringBuilder buildPageNumber = new StringBuilder();

            try {
                String s = getString(url, sb);

                System.out.println("Количество символов: " + s.length());

                final String SEARCHFIRST = "__info";
                final String SEARCHLAST = "</div></div></div>";

                int start, end;

                searchParam.getPageNumber(pageNumber, searchLine, s, buildPageNumber);

                while ((s.contains(SEARCHFIRST))) {
                    start = s.indexOf(SEARCHFIRST) + 8;
                    end = s.indexOf(SEARCHLAST, start);

                    s.substring(start, end);

                    searchParam.getPrice(listNum, s, priceBuild);

                    searchParam.getShop(listShop, s, shopBuild);

                    searchParam.getLinkShop(listLinkShop, s, linkShopBuild);

                    searchParam.getName(listNameProd, s, nameProdBuild);

                    buildPage.delete(0, Integer.MAX_VALUE);
                    buildPage.append(s);

                    buildPage.delete(start - 8, end + 1);

                    s = buildPage.toString();
                }

            } catch (Exception e) {

            }

            pages = pageNumber.get(0);

            System.out.println(listNum);
            System.out.println(listShop);
            System.out.println(listNameProd);

            System.out.println("Количество элементов: " + listNum.size());
            System.out.println("Количество элементов: " + listShop.size());
            System.out.println("Количество страниц: " + pageNumber.get(0));
            System.out.println("Количество элементов: " + listNameProd.size());
        }
    }

    private String getString(URL url, StringBuilder sb) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        char[] buf = new char[1000];
        int rest;

        do {
            if ((rest = bufferedReader.read(buf)) > 0)
                sb.append(new String(buf, 0, rest));
        }
        while (rest > 0);
        return sb.toString();
    }

    public class SearchParam {

        public void getPrice(ArrayList listNum, String s, StringBuilder priceBuild) {
            int startNum;
            int endNum;
            String sNum;

            startNum = s.indexOf("__num") + 7;
            endNum = s.indexOf("</", startNum);

            sNum = s.substring(startNum, endNum);
            listNum.add(sNum);

            priceBuild.delete(0, Integer.MAX_VALUE);
            priceBuild.append(s);

            priceBuild.delete(startNum - 7, endNum + 1);
        }

        public void getLinkShop(ArrayList listLinkShop, String s, StringBuilder linkShopBuild) {
            int startLinkShop;
            int endLinkShop;
            String sLinkShop;

            final String SearchShopLinkFirst = "p-link\" href=";
            int searchShopLinkFirst = SearchShopLinkFirst.length();

            startLinkShop = s.indexOf(SearchShopLinkFirst) + searchShopLinkFirst + 1;
            endLinkShop = s.indexOf("\">", startLinkShop);

            sLinkShop = s.substring(startLinkShop, endLinkShop);

            listLinkShop.add(sLinkShop.replace("amp;", ""));

            linkShopBuild.delete(0, Integer.MAX_VALUE);
            linkShopBuild.append(s);

            linkShopBuild.delete(startLinkShop - (searchShopLinkFirst + 1), endLinkShop + 1);
        }

        public void getName(ArrayList listNameProd, String s, StringBuilder nameProdBuild) {
            int startNameProd;
            int endNameProd;
            String sNameProd;

            final String SearchShopLinkFirst = "p-link\" href=";
            int searchShopLinkFirst = SearchShopLinkFirst.length();

            startNameProd = (s.indexOf("s__name") + 10);
            endNameProd = (s.indexOf("</h3>", startNameProd));

            sNameProd = s.substring(startNameProd, endNameProd);

            String trimName = sNameProd.substring(sNameProd.indexOf(">") + 1, sNameProd.indexOf("</a>"));
            String delB = trimName.replace("<b>", "");

            listNameProd.add(delB.replace("</b>", ""));

            nameProdBuild.delete(0, Integer.MAX_VALUE);
            nameProdBuild.append(s);

            nameProdBuild.delete(startNameProd - (searchShopLinkFirst + 1), endNameProd + 1);
        }

        public void getShop(ArrayList listShop, String s, StringBuilder shopBuild) {
            int startShop;
            int endShop;
            String sShop;

            startShop = s.indexOf("=shpnm") + 8;
            endShop = s.indexOf("</a>", startShop);

            sShop = s.substring(startShop, endShop);
            listShop.add(sShop);

            shopBuild.delete(0, Integer.MAX_VALUE);
            shopBuild.append(s);

            shopBuild.delete(startShop - 8, endShop + 1);
        }

        private void getPageNumber(ArrayList pageNumber, String searchLine, String s, StringBuilder buildPageNumber) {
            int startPageNumber = s.indexOf("h-stat\">") + 8;
            int endPageNumber = s.indexOf(".", startPageNumber);

            String numberLine = s.substring(startPageNumber, endPageNumber);

            String numberPage = numberLine.replace("«" + searchLine + "» — ", "");
            int pageNumer = Integer.parseInt(numberPage);
            int page;

            if ((pageNumer % 10) > 0) {
                page = (pageNumer / 10) + 1;
            } else
                page = (pageNumer / 10);

            pageNumber.add(0, page);

            buildPageNumber.delete(0, Integer.MAX_VALUE);
            buildPageNumber.append(s);

            buildPageNumber.delete(startPageNumber - 8, endPageNumber + 1);
        }
    }
}
