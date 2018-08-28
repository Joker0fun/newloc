package lx.newloc.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class FtpUtils
{
	private static FTPClient ftpClient = new FTPClient();
	/**
	 * 向FTP服务器上传文件
	 *
	 * @param url
	 *            FTP服务器hostname
	 * @param port
	 *            FTP服务器端口
	 * @param username
	 *            FTP登录账号
	 * @param password
	 *            FTP登录密码
	 * @param path
	 *            FTP服务器保存目录,如果是根目录则为“/”
	 * @param filename
	 *            上传到FTP服务器上的文件名
	 * @param input
	 *            本地文件输入流
	 * @return 成功返回true，否则返回false
	 */
	public static boolean uploadFile(String url, int port, String userName, String password,String path, String fileName, InputStream input)
	{
		if (!path.startsWith("/"))
		{
			path = "/" + path;
		}
		if (!path.endsWith("/"))
		{
			path = path + "/";
		}
		boolean result = false;
		if (connectFtp(url, port, userName, password))
		{
			try
			{
				String directory = path;
				if (!directory.equalsIgnoreCase("/")
						&& !ftpClient.changeWorkingDirectory(directory))
				{
					// 如果远程目录不存在，则递归创建远程服务器目录
					int start = 0, end = 0;

					if (directory.startsWith("/"))
					{
						start = 1;
					} else
					{
						start = 0;
					}

					end = directory.indexOf("/", start);

					while (true)
					{

						String subDirectory = path.substring(start, end);

						if (!ftpClient.changeWorkingDirectory(subDirectory))
						{

							if (ftpClient.makeDirectory(subDirectory))
							{

								ftpClient.changeWorkingDirectory(subDirectory);

							} else
							{
								System.out.println("create directory failed!");
								return false;
							}
						}
						start = end + 1;
						end = directory.indexOf("/", start);
						// 检查所有目录是否创建完毕
						if (end <= start)
						{
							break;
						}
					}
				}
				ftpClient.setFileType( FTP.BINARY_FILE_TYPE);
				//ftpClient.setControlEncoding( "gbk" );
				result = ftpClient.storeFile(new String(fileName.getBytes(DirFileUtils.encoding), DirFileUtils.encoding),
						input);
				if (result)
				{
					System.out.println("上传成功!");
				}
				input.close();
				ftpClient.logout();
			} catch (IOException e)
			{
				e.printStackTrace();
			} finally
			{
				if (ftpClient.isConnected())
				{
					try
					{
						ftpClient.disconnect();
						//ftpClient = null;
					} catch (IOException ioe)
					{
						//ioe.printStackTrace();
					}
				}
			}
		}
		return result;
	}

	/**
	 * 连接Ftp服务器
	 * 
	 * @param url
	 * @param username
	 * @param password
	 * @return 连接成功返回true，否则返回false
	 */
	private static boolean connectFtp(String url, int port, String username, String password)
	{
		boolean result = false;
		int reply;
		try
		{
			ftpClient.connect(url, port);
			ftpClient.login(username, password);
			ftpClient.setControlEncoding(DirFileUtils.encoding);
			reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply))
			{
				ftpClient.disconnect();
				result = false;
			} else
			{
				result = true;
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 将本地文件上传到FTP服务器上
	 *
	 */
	public void testUpLoadFromDisk()
	{
		try
		{
			FileInputStream in = new FileInputStream(new File("E:/号码.txt"));
			boolean flag = uploadFile("221.178.187.38", 21, "chigoo_qingdao",
					"chigoo_qingdaochigoo_qingdao", "/", "哈哈.txt", in);
			System.out.println(flag);
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 从FTP服务器下载文件
	 *
	 * @param url
	 *            FTP服务器hostname
	 * @param port
	 *            FTP服务器端口
	 * @param username
	 *            FTP登录账号
	 * @param password
	 *            FTP登录密码
	 * @param remotePath
	 *            FTP服务器上的相对路径
	 * @param fileName
	 *            要下载的文件名
	 * @param localPath
	 *            下载后保存到本地的路径
	 * @return
	 */
	public static boolean downloadFile(String url, int port, String username, String password,String remotePath, String fileName, String localPath)
	{
		boolean result = false;
		String  RemoteDownFileName=remotePath+fileName;
		if (!RemoteDownFileName.startsWith("/")) 
		{
			RemoteDownFileName="/"+RemoteDownFileName;
		}
		if (connectFtp(url, port, username, password))
		{
			try
			{
				// 转移到FTP服务器目录至指定的目录下
				ftpClient.changeWorkingDirectory(new String(remotePath.getBytes(DirFileUtils.encoding), DirFileUtils.encoding));
				File localFile = new File(localPath + "/" +fileName);
				DirFileUtils.CreateAllDirs(localFile.toString(), true);
				OutputStream is = new FileOutputStream(localFile);
				result = ftpClient.retrieveFile(RemoteDownFileName, is);
				is.close();
				ftpClient.logout();
			} catch (IOException e)
			{
				e.printStackTrace();
			} finally
			{
				if (ftpClient.isConnected())
				{
					try
					{
						ftpClient.disconnect();
						ftpClient = null;
					} catch (IOException ioe)
					{
						ioe.printStackTrace();
					}
				}
			}
		}
		return result;
	}

	/**
	 * 将FTP服务器上文件下载到本地
	 *
	 */
	public void testDownFile()
	{
		try
		{
			boolean flag = downloadFile("221.178.187.38", 21, "chigoo_qingdao",
					"chigoo_qingdaochigoo_qingdao", "/", "ChigooTcpChannel.jar", "D:/");
			System.out.println(flag);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 上传文件或者文件夹
	 * 
	 * @param url
	 * @param port
	 * @param username
	 * @param password
	 * @param remotePath
	 * @param file
	 *            上传的文件或文件夹
	 * @return 上传成功返回true，否则返回false
	 */
	public static boolean upload(String url, int port, String username, String password,
			String remotePath, File file)
	{
		boolean result = false;
		if (connectFtp(url, port, username, password))
		{
			try
			{
				if (file.isDirectory())
				{
					ftpClient.makeDirectory(file.getName());
					ftpClient.changeWorkingDirectory(file.getName());
					String[] files = file.list();
					for (int i = 0; i < files.length; i++)
					{
						File file1 = new File(file.getPath() + "/" + files[i]);
						if (file1.isDirectory())
						{
							upload(url, port, username, password, remotePath, file1);
							ftpClient.changeToParentDirectory();
						} else
						{
							File file2 = new File(file.getPath() + "/" + files[i]);
							FileInputStream input = new FileInputStream(file2);
							ftpClient.storeFile(file2.getName(), input);
							input.close();
						}
					}
				} else
				{
					File file2 = new File(file.getPath());
					FileInputStream input = new FileInputStream(file2);
					ftpClient.storeFile(file2.getName(), input);
					input.close();
				}
			} catch (IOException e)
			{
				result = false;
			}
			result = true;
		}

		return result;
	}

	/**
	 * 删除远程FTP文件
	 * 
	 * @param remote
	 *            远程文件路径
	 * @return
	 */
	public static boolean deleteFile(String url, int port, String username, String password,
			String remote)
	{
		boolean result = false;
		FTPFile[] files;
		if (connectFtp(url, port, username, password))
		{
			try
			{
				ftpClient.enterLocalPassiveMode();
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
				ftpClient.changeWorkingDirectory(remote);
				ftpClient.changeToParentDirectory();
				files = ftpClient.listFiles(remote);
//				for(FTPFile file : files)
//				{
//					System.out.println(file.getName());
//				}
				if (files.length == 1)
				{
					result = ftpClient.deleteFile(remote);
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return result;
	}

	public static void main(String[] args)
	{
		FtpUtils fa = new FtpUtils();
//		 fa.testDownFile();
//		fa.testUpLoadFromDisk();
		fa.testDeleteFile();
	}

	private void testDeleteFile()
	{
		try
		{
			boolean flag = deleteFile("221.178.187.38", 21, "chigoo_qingdao",
					"chigoo_qingdaochigoo_qingdao", "/download/3个机场清单/广州/local_list_full.txt");
			System.out.println(flag);
		} catch (Exception e)
		{
			e.printStackTrace();
		}		
	}
}