package conf;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormat extends Formatter
{
	private UUID procID = null;

	public LogFormat()
	{
		this.procID = UUID.randomUUID();
	}

	public String format(LogRecord record)
	{
		SimpleDateFormat Dateformatter = new SimpleDateFormat("dd/MM/yyyy|HH:mm:ss.SSS");
		java.util.Formatter MessageFormat = new java.util.Formatter();
		try
		{
			Object[] arguments = { new String(procID.toString()), new String(record.getLevel().toString()), new String(record.getSourceMethodName()),
					new String(Dateformatter.format(new Date(record.getMillis()))), new String(formatMessage(record)), };

			MessageFormat.format("%s|%-10s|%-40s|%s|%s\n", arguments);
			return MessageFormat.toString();
		} finally
		{
			MessageFormat.close();
		}
	}

}