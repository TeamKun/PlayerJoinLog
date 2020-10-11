package net.teamfruit.playerjoinlog;

import com.google.common.base.Charsets;
import com.velocitypowered.api.proxy.Player;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ログ
public class Log implements AutoCloseable {
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss.n");

    private File path;
    private CSVPrinter writer;

    public Log(File path) throws IOException {
        this.path = path;

        CSVFormat format = CSVFormat.EXCEL;
        if (!path.exists())
            format = format.withHeader("種類", "時間", "Discord ID", "Discord 名前");
        format = format.withQuoteMode(QuoteMode.MINIMAL);
        writer = format.print(new OutputStreamWriter(new FileOutputStream(path, true), Charsets.UTF_8));
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    public void log(LocalDateTime time, Player player, boolean joined) throws IOException {
        writer.printRecord(joined ? "参加" : "退出", formatter.format(time), player.getUniqueId().toString(), player.getUsername());
        writer.flush();
    }
}
