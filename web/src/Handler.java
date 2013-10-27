import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpMethod.GET;

public class Handler extends SimpleChannelInboundHandler<Object> {

    ArrayList logList = new ArrayList();
    Log getLog = new Log();
    String uri;
    private int reqCount = 0;
    private int redCount = 0;
    private HashMap<String, ReqInfo> reqStat = new HashMap<String, ReqInfo>();
    private HashMap<String, RedInfo> redStat = new HashMap<String, RedInfo>();

    private static void sendHttpResponse(
            ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            setContentLength(res, res.content().readableBytes());
        }

        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {

        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.MOVED_PERMANENTLY);
        response.headers().set(HttpHeaders.Names.LOCATION, newUri);
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static String getWebLocation(FullHttpRequest req) {
        return req.headers().get(HOST);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            channelRead(ctx, (FullHttpRequest) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    public String reqStatToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");

        Set<String> keys_rqi = reqStat.keySet();
        Set<String> keys_rdi = redStat.keySet();

        ReqInfo rqi;
        RedInfo rdi;

        for (String s : keys_rqi) {
            sb.append("<table border=\"3\"><tr>");

            rqi = reqStat.get(s);

            sb.append("<tr><th>").append("Количество уникальных запросов").append("</th>");
            sb.append("<th>").append("IP Address").append("</th>");
            sb.append("<th>").append("Количество запросов").append("</th>");
            sb.append("<th>").append("Время последнего запроса").append("</th></tr>");
            sb.append("<tr><td>").append(reqCount).append("</td>");
            sb.append("<td>").append(s).append("</td>");
            sb.append("<td>").append(rqi.count).append("</td>");
            sb.append("<td>").append(new Date(rqi.time).toString()).append("</td></tr>");
            sb.append("</tr></table>");
        }

        for (String st : keys_rdi) {
            sb.append("<table border=\"3\"><tr>");

            rdi = redStat.get(st);

            sb.append("<tr><th>").append("Общее количество редиректов").append("</th>");
            sb.append("<th>").append("URL редирект").append("</th>");
            sb.append("<tr><td>").append(redCount).append("</td>");
            sb.append("<td>").append(rdi.redURL).append("</td></tr>");
            sb.append("</tr></table>");
        }

        sb.append("</html>");

        return sb.toString();

    }

    private String logToString() throws IOException, ClassNotFoundException {

        StringBuilder sb = new StringBuilder();

        sb.append("<html><head>")
                .append("</head><body>").append("<table border=\"3\">")
                .append("<tr><th>").append("#").append("</th>")
                .append("<th>").append("src_ip:").append("</th>")
                .append("<th>").append("URI").append("</th>")
                .append("<th>").append("Timestamp").append("</th>")
                .append("<th>").append("sent_bytes").append("</th>")
                .append("<th>").append("received_bytes").append("</th>")
                .append("<th>").append("speed (bytes/sec)").append("</th></tr>");
        for (int i = 0; i < logList.size(); i++) {
            sb.append("<tr><td>").append(i).append("</td>")

                    .append("<td>").append(logList.get(i)).append("</td>");
        }

        sb.append("</tr></table>").append("</body></html>");

        return sb.toString();

    }

    public void channelRead(ChannelHandlerContext ctx, FullHttpRequest request)
            throws Exception {
        StringBuilder html = new StringBuilder();
        Date date = new Date();

        uri = request.getUri();

        logList.add(html.append(getLog.src_ip = InetAddress.getByAddress(InetAddress.getLocalHost().getAddress())).append("</td>")
                .append("<td>").append(getLog.uri = uri).append("</td>")
                .append("<td>").append(getLog.timeStamp = new Timestamp(date.getTime())).append("</td>")
                .append("<td>").append(getLog.send_byte).append("</td>")
                .append("<td>").append(getLog.received_byte).append("</td>")
                .append("<td>").append(getLog.speed).append("</td>"));

        final QueryStringDecoder dec = new QueryStringDecoder(uri);
        String redURL = null;

        if (!request.getDecoderResult().isSuccess()) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        if (request.getMethod() != GET) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
            return;
        }
        if (dec.path().equals("/")) {
            ByteBuf content = MainPage.getContent(getWebLocation(request));

            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            sendHttpResponse(ctx, request, response);
            return;
        }

        if (dec.path().equals("/hello")) {
            ByteBuf contentH = MainPage.getHelloPage(getWebLocation(request));
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, contentH);
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());

            sendHttpResponse(ctx, request, response);
            return;
        }

        if (dec.path().equals("/redirect")) {
            redURL = dec.parameters().get("url").get(0);
            redCount++;
            sendRedirect(ctx, redURL);
        }

        if (dec.path().equals("/status")) {

            String ip = ctx.channel().localAddress().toString();

            int idx = ip.indexOf(':');
            ip = ip.substring(0, idx);

            reqCount++;

            ReqInfo info = reqStat.get(ip);
            if (info == null)
                reqStat.put(ip, new ReqInfo(1));
            else
                reqStat.put(ip, new ReqInfo(info.count + 1));

            /*-------------- Redirect info ---------------------*/

            RedInfo redInfo = redStat.get(redURL);
            if (redInfo == null)
                redStat.put(redURL, new RedInfo(1, redURL));
            else
                redStat.put(redURL, new RedInfo((redInfo.count + 1), redURL));

            /*-------------- Redirect info ---------------------*/

            ByteBuf contentStat = Unpooled.copiedBuffer(reqStatToString(), CharsetUtil.UTF_8);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, contentStat);
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html");
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            sendHttpResponse(ctx, request, response);
        }

        if (dec.path().equals("/log")) {

            ByteBuf contentLog = Unpooled.copiedBuffer(logToString(), CharsetUtil.UTF_8);

            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, contentLog);
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html");
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            sendHttpResponse(ctx, request, response);

        }

        return;

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    static class ReqInfo {
        int count = 0;
        long time;

        public ReqInfo(int count) {
            this.count = count;
            time = System.currentTimeMillis();
        }
    }

    static class RedInfo {

        String redURL;
        int count = 0;

        public RedInfo(int count, String url) {
            this.count = count;
            //this.redURL = url;
        }
    }

    public class Log {
        InetAddress src_ip;
        String uri;
        Timestamp timeStamp;
        int send_byte;
        int received_byte;
        double speed;

        public void setSrc_ip(InetAddress src_ip) throws UnknownHostException {
            this.src_ip = src_ip;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public void setTimeStamp(Timestamp timeStamp) {
            this.timeStamp = timeStamp;
        }

        public void setSend_byte(int send_byte) {
            this.send_byte = send_byte;
        }

        public void setReceived_byte(int received_byte) {
            this.received_byte = received_byte;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }
    }
}
