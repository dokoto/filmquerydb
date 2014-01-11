package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import com.fasterxml.jackson.databind.ObjectMapper;

import conf.ConfigBuilder;

public class FilmPlay
{
	private static final String link = "http://mansierra.homelinux.net:8080/fqdb/streamLink.m3u";
	private static Thread thr_ffServer = null;
	private static Thread thr_ffmpeg = null;

	public static String ManageQueryBy(HttpServletRequest request)
	{
		final ConfigBuilder GlobConf = (ConfigBuilder) request.getSession().getAttribute("GlobConf");
		final String path = request.getParameter("path");
		GlobConf.Log().info("Playing movie from : " + path);

		try
		{
			if (thr_ffmpeg != null)
			{				
				if (thr_ffmpeg.isAlive())
				{
					thr_ffmpeg.interrupt();
					thr_ffmpeg = null;
				}
			}

			if (thr_ffServer != null)
			{				
				exe(GlobConf, "rm", "/home/administrador", new String[] { "-rf", "/tmp/feed1.ffm" });
				if (thr_ffServer.isAlive())
				{
					thr_ffServer.interrupt();
					thr_ffServer = null;
				}
			}
									
			thr_ffServer = new Thread()
			{
				public void run()
				{
					try
					{
						//killAll(GlobConf, "ffserver");
						GlobConf.Log().info("Starting FFSERVER");
						exe(GlobConf, "/opt/ffmpeg_sources/ffmpeg/./ffserver", "/home/administrador",
								new String[] { "-f", "/etc/ffserver.conf" });
						GlobConf.Log().info("Terminating server FFSERVER");
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
						//killAll(GlobConf, "ffmpeg");
						GlobConf.Log().info("Starting FFMPEG with : " + path);
						exe(GlobConf, "/opt/ffmpeg_sources/ffmpeg/./ffmpeg -i \"" + path + "\" http://localhost:51413/feed1.ffm");
						GlobConf.Log().info("Terminating FFMPEG with : " + path);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			};
			thr_ffmpeg.start();
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

	private static boolean exeNoWait(ConfigBuilder GlobConf, String fullCmd) throws IOException, InterruptedException
	{

		Runtime r = Runtime.getRuntime();
		Process p = r.exec(fullCmd);
		BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
		System.out.println(b.readLine());
		GlobConf.Log().info(b.readLine());

		return true;
	}

	private static boolean exe(ConfigBuilder GlobConf, String fullCmd) throws IOException, InterruptedException
	{

		Runtime r = Runtime.getRuntime();
		Process p = r.exec(fullCmd);
		// p.waitFor();
		BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = "";

		while ((line = b.readLine()) != null)
		{
			System.out.println(line);
			GlobConf.Log().info(line);
		}
		return true;
	}

	private static long exe(ConfigBuilder GlobConf, String command, String workingDir, String[] arguments) throws Exception
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
			GlobConf.Log().info(out.getOutput());
			GlobConf.Log().info(err.getOutput());
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

	public static void killAll(ConfigBuilder GlobConf, String process)
	{
		try
		{
			Vector<String> commands = new Vector<String>();
			commands.add("pidof");
			commands.add(process);
			ProcessBuilder pb = new ProcessBuilder(commands);
			Process pr = pb.start();
			pr.waitFor();
			if (pr.exitValue() != 0)
				return;
			BufferedReader outReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			for (String pid : outReader.readLine().trim().split(" "))
			{
				GlobConf.Log().info("Killing pid: " + pid);
				Runtime.getRuntime().exec("kill " + pid).waitFor();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

}
