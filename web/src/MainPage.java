import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class MainPage {
    public static ByteBuf getContent(String getWebLocation) {
        return Unpooled.copiedBuffer(
                "1. Request --> http://somedomain/hello --> <<Hello World>> after 10 seconds" + "\r\n" +
                        "2. Request --> http://somedomain/redirect?url=<url> --> Redirection" + "\r\n" +
                        "3. Request --> http://somedomain/status --> Statistic" + "\r\n" +
                        "4. http://somedomain/log --> Log",
                CharsetUtil.UTF_8);
    }

    public static ByteBuf getHelloPage(String getWebLocation) throws InterruptedException {
        Thread.sleep(10000);
        return Unpooled.copiedBuffer("<<Hello World>>", CharsetUtil.UTF_8);
    }


}


