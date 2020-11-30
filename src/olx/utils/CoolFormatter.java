package olx.utils;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CoolFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%1$tl:%1$tM:%1$tS.%1$tL %2$s %3$s%n", record.getMillis(), record.getSourceClassName(), record.getSourceMethodName()));
        
        if(!record.getLevel().getName().equals("INFO"))
            sb.append(record.getLevel()).append(':');
        sb.append(record.getMessage()).append('\n');
        return sb.toString();
    }
    
}