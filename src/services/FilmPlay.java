package services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

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
				
				if (thr_ffServer.isAlive())
				{
					thr_ffServer.interrupt();
					thr_ffServer = null;
				}
			}
			
			exe(GlobConf, new String[] { "rm", "-rf", "/tmp/feed1.ffm" });
			killAll(GlobConf, "ffmpeg");
			killAll(GlobConf, "ffserver");

			thr_ffServer = new Thread()
			{
				public void run()
				{
					try
					{						
						exe(GlobConf, new String[] { "/opt/ffmpeg_sources/ffmpeg/ffserver", "-f", "/etc/ffserver.conf" });
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
						exe(GlobConf, new String[] { "/opt/ffmpeg_sources/ffmpeg/ffmpeg", "-i", path, "http://localhost:51413/feed1.ffm" });
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

	private static boolean exe(ConfigBuilder GlobConf, String[] fullCmd) throws IOException, InterruptedException
	{
		Runtime r = Runtime.getRuntime();
		GlobConf.Log().info("Iniciando comando : " + fullCmd[0]);
		Process p = r.exec(fullCmd);
		p.waitFor();
		GlobConf.Log().info("Terminado comando : " + fullCmd[0]);

		if (p.getErrorStream() != null)
			printLOG(GlobConf, p.getErrorStream());
		if (p.getInputStream() != null)
			printLOG(GlobConf, p.getInputStream());

		return true;
	}

	private static void printLOG(ConfigBuilder GlobConf, InputStream ip) throws IOException
	{
		BufferedReader b = new BufferedReader(new InputStreamReader(ip));
		String line = "";

		while ((line = b.readLine()) != null)
		{
			System.out.println(line);
			GlobConf.Log().info(line);
		}
	}

	public static void killAll(ConfigBuilder GlobConf, String process) throws IOException, InterruptedException
	{
		GlobConf.Log().info("Matando comando : " + process);
		Vector<String> commands = new Vector<String>();
		commands.add("pidof");
		commands.add(process);
		ProcessBuilder pb = new ProcessBuilder(commands);
		Process pr = pb.start();
		pr.waitFor();		
		if (pr.exitValue() != 0)
		{
			GlobConf.Log().info("El proceso no existe " +  process);
			return;
		}
		BufferedReader outReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		for (String pid : outReader.readLine().trim().split(" "))
		{
			GlobConf.Log().info("Killing pid: " + pid);			
			Runtime r = Runtime.getRuntime();
			Process p = r.exec("kill " + pid);
			p.waitFor();
			if (p.getErrorStream() != null)
				printLOG(GlobConf, p.getErrorStream());
			if (p.getInputStream() != null)
				printLOG(GlobConf, p.getInputStream());
		}

	}

}
