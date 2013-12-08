package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FilmPlay
{
	private static final String link = "http://mansierra.homelinux.net:8080/fqdb/streamLink.m3u";
	private static Thread threadON = null;
	private static Thread thr_ffServer = null;
	private static long pid_ffserver = -1;
	private static Thread thr_ffmpeg = null;

	public static String ManageQueryBy(HttpServletRequest request)
	{
		final String path = request.getParameter("path");
		try			
		{
			if (pid_ffserver != -1)
				exe("kill", "/home/administrador", new String[] {"-9", String.valueOf(pid_ffserver)});
			if (thr_ffServer != null)
			{
				if (thr_ffServer.isAlive())
				{
					thr_ffServer.interrupt();
					thr_ffServer = null;
				}
			}
			
			if (thr_ffmpeg != null)
			{
				if (thr_ffmpeg.isAlive())
				{
					thr_ffmpeg.interrupt();
					thr_ffmpeg = null;
				}
			}
			
			thr_ffServer = new Thread()
			{
				public void run()
				{
					try
					{
						pid_ffserver = exe("/opt/bin/ffserver", "/home/administrador", new String[] {"-f", "/etc/ffserverNoDaemon.conf"});
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			};
			thr_ffServer.start();
			Thread.sleep(1000);
			thr_ffmpeg = new Thread()
			{
				public void run()
				{
					try
					{
						exe("/opt/bin/ffmpeg", "/home/administrador", new String[] {"-i", path, "http://localhost:51413/feed1.ffm"});
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			};
			thr_ffmpeg.start();
			
			/*
			if (threadON != null)
			{
				if (threadON.isAlive())
				{
					threadON.interrupt();
					threadON = null;
				}
			}
			threadON = new Thread()
			{
				public void run()
				{
					try
					{
						exe("/usr/local/bin/sffserver", "/home/administrador", new String[] { path });
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			};
			threadON.start();
			Thread.sleep(1000);
			*/
			Map<String, Object> root = new HashMap<String, Object>();
			root.put("url", link);
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsString(root);

		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

	}

	private static boolean exeNoWait(String fullCmd) throws IOException, InterruptedException
	{

		Runtime r = Runtime.getRuntime();
		Process p = r.exec(fullCmd);
		BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
		System.out.println(b.readLine());

		return true;
	}

	private static boolean exe(String fullCmd) throws IOException, InterruptedException
	{

		Runtime r = Runtime.getRuntime();
		Process p = r.exec(fullCmd);
		p.waitFor();
		BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = "";

		while ((line = b.readLine()) != null)
		{
			System.out.println(line);
		}

		return true;
	}

	private static long exe(String command, String workingDir, String[] arguments) throws Exception
	{
		Commandline commandline = new Commandline();
		commandline.setExecutable(command);
		commandline.addArguments(arguments);
		commandline.setWorkingDirectory(new File(workingDir));

		CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
		CommandLineUtils.StringStreamConsumer out = new CommandLineUtils.StringStreamConsumer();

		int exitCode;
		try
		{
			exitCode = CommandLineUtils.executeCommandLine(commandline, out, err, 10);
			
		} catch (CommandLineException e)
		{
			e.printStackTrace();
			return -1;
		}

		String output = out.getOutput();
		if (!StringUtils.isEmpty(output))
		{
			System.out.println(output);
			return -1;
		}

		String error = err.getOutput();
		if (!StringUtils.isEmpty(error))
		{
			System.out.println(error);
			return -1;
		}

		return commandline.getPid();
	}

}
