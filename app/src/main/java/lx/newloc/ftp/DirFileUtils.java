package lx.newloc.ftp;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Stack;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2011</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DirFileUtils {
	public final static String encoding = "utf-8";
   	static byte[] crcbuf = new byte[8192];
   	static byte[] copybuf= new byte[8192];

   	public DirFileUtils() {
    }
    
    static private  long doChecksum(String fileName) {
           long   Checksum=0xffffffff;
           CheckedInputStream cis = null;
           FileInputStream     in = null;
           try {
   	            try 
   	            {
   	            	 in   = new FileInputStream(fileName);
   	            	 
   	            } catch (FileNotFoundException e) 
   	            {

   	            	return  Checksum;
   	            }
	            cis  = new CheckedInputStream(in, new CRC32());
   	            while(cis.read(crcbuf)  !=-1)
   	            {
   	            	
   	            }
   	            Checksum= cis.getChecksum().getValue();
   	            in.close();
   	            cis.close();
   	            cis=null;
   	            in=null;
           } catch (IOException e) {}
           
           return Checksum;
       }

    synchronized static public void ScanDisk(String ScanDir,List <String>  SendList)
    {
    	
    	String fileName;
    	String Conext;
    	int    RootDirLen=ScanDir.length();
    	Stack<String>   stack = new Stack<String>();
        stack.push(ScanDir);
    	SendList.clear();
    	
    	while(!stack.isEmpty()){
    	       File rootDir = new File((String) stack.pop());
    	       if (rootDir.exists()){
    	           String[] fileList =  rootDir.list();
    	           for (int i = 0; i < fileList.length; i++) {
    	               File   subfile=null;
//    	               fileName = rootDir.getAbsolutePath()+"/"+fileList[i];
//    	               fileName=fileName.replace("\\","/");
    	               fileName = rootDir.getAbsolutePath() + File.separator + fileList[i];
    	               subfile= new File(fileName);
    	               if (!subfile.isDirectory()){
    	                   if (!fileName.contains("Thumbs.db")){
    	                         Conext=fileName.substring(RootDirLen)+"@"+subfile.length()+"@"+doChecksum(fileName);
    	                         SendList.add(Conext);
    	                   }else
    	                   {
    	                         subfile.delete();
    	                   }
    	               }else
    	               {
    	                   stack.push(fileName);
    	               }
    	               subfile=null;
    	         }
    	       }
    	   }    	
    }
    
        
    synchronized static public  void CreateAllDirs(String fileName,boolean hasfile)
    {
        File file = new File(fileName);
        file.getParentFile().mkdirs();
        if (hasfile==false)  file.mkdir();
        file=null;
    }
    synchronized static  public  void MoveFile(String DestFileName,String SourceFile) 
    {
        File   srcFile   =   new   File(SourceFile);    
        File   dtnFile   =   new   File(DestFileName);  
        if (srcFile.exists()) {
        	  if (dtnFile.exists())
        	  {
    		       dtnFile.delete();
    	      }else
    	      {
    	    	   dtnFile.getParentFile().mkdirs();  
    	      }
              srcFile.renameTo(dtnFile);
        }
        dtnFile=null;
        srcFile=null;
    }
    static public boolean copyFileTo(String fileFrom, String fileTo) {  
        try {
     	   File     dtnFile   =   new File(fileTo);
     	   if (dtnFile.exists())
     	   {
     		   dtnFile.delete();    		   
     	   }
     	   dtnFile=null;
     	   
     	   FileInputStream  in  = new FileInputStream(fileFrom);
     	   RandomAccessFile out = new RandomAccessFile(fileTo,"rw");
     	   int  count;  
     	   out.seek(0);
     	   while ((count = in.read(copybuf)) !=-1)
     	   {  
     	         out.write(copybuf, 0, count);
     	   }
     	   out.close();
     	   in.close();
     	   
     	   try {Thread.sleep(1*1000);} catch (InterruptedException e) {}
     	   
     	   out=null;
     	   in=null;
     	   return true;
     	   } catch (IOException ex){  
  		       ex.printStackTrace();
  		       return false;  
     	   }  
     }  

    synchronized static public void DeleteDirectoryFiles(String Root)
    {
        String fileName;
        Stack   stack = new Stack();
        stack.push(Root);
        try {
        	while(!stack.isEmpty()){
        		File rootDir = new File((String) stack.pop());
        		String[] fileList =  rootDir.list();
        		for (int i = 0; i < fileList.length; i++) {
                     File     subfile=null;
//                     fileName = rootDir.getAbsolutePath()+"/"+fileList[i];
//                     fileName=fileName.replace("\\","/");
                     fileName = rootDir.getAbsolutePath() + File.separator + fileList[i];
                     subfile= new File(fileName);
                     if (!subfile.isDirectory()){
                    	     subfile.delete();
                     }else {
                           stack.push(fileName);
                     }
                    subfile=null;
                    }
        	}
      }catch (Exception ex){}
  }  
    
    synchronized static public boolean TextIsEmpty(String textInfo)
    {
     if (textInfo== null ||"".equalsIgnoreCase(textInfo)) return true;
     return false;
    }
    
    synchronized static public byte[] readFileFromSource(String FileName)
    {
        File file = new File(FileName);
        byte[] bytes = new byte[(int) file.length()];
        InputStream is;
		try {
			is = new FileInputStream(file);
	        try {
				is.read(bytes);
		        is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        return bytes;
    }
    synchronized static public byte[] readFileFromSource(String FileName,long StartPost,long BlockSize,long FileSize)
    {
        File file = new File(FileName);
        int  ReadBlock; 
        if (StartPost+BlockSize<=FileSize)
        	   ReadBlock=(int) BlockSize;
        else   ReadBlock=(int) (FileSize-StartPost);
        byte[] bytes = new byte[ReadBlock];
        InputStream is;
        try {
			is = new FileInputStream(file);
			is.skip(StartPost);
			is.read(bytes);
	        is.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
        return bytes;
    }
    
    synchronized static public void writeFileToTmpTarget(String targetFile, byte[] bytes,long StartPost) throws Exception
    {
       File file=new File(targetFile);
       file.getParentFile().mkdirs();
       file=null;

       RandomAccessFile os =new RandomAccessFile(targetFile, "rw");
       os.seek(StartPost);
       os.write(bytes);
       os.close();
       os=null;
    }


    synchronized public static boolean fileIsExists(String ifileName){
	   File f=new File(ifileName);
       if(!f.exists()){
               return false;
       }
       return true;
    }
    
    synchronized public static boolean pathIsAbs(String ipath){
    	if (ipath.length()>=2 &&ipath.charAt(1)==':' || (ipath.length()>=1 &&ipath.charAt(0)==File.separatorChar))
		{
    		return true;
		}
    	return false;
    }
    
    synchronized public static void DeleteFile(String FileName)
    {
		   File f=new File(FileName);
	       if(f.exists())
	               f.delete();
	}
    
    
    synchronized public static String PathToStandardPath(String ipath)
    {
    	String path = ipath;

    	if (path==null) return "";
    	
    	if (path.length()>0 &&  path.charAt(path.length()-1)!='/' && path.charAt(path.length()-1)!='\\')
		{
    		path= path+File.separator;
    	}
    	path=path.replace("/" , File.separator);
    	path=path.replace("\\", File.separator);
    	
    	return path;    	
    }
    synchronized public static String PathToStandardFileName(String iFileName)
    {   String  FileName=iFileName;
    
    	if (FileName==null) return "";
    	
    	FileName=FileName.replace("/" , File.separator);
    	FileName=FileName.replace("\\", File.separator);
    	
    	return FileName;    	
    }
    public static String  getFileOfPathFile(String PathFileName)
    {
        return PathFileName.substring(PathFileName.lastIndexOf(File.separator)+1);  
    }
    
    public static boolean  FileIsOpen(String FileName)
	{
       boolean  Result=false;
       try 
       {
		 RandomAccessFile os =new RandomAccessFile(FileName, "rws");
		 os.close();
	   } catch (FileNotFoundException e) 
	   {
		   Result=true;
		   
	   }catch (IOException e) 
	   {
		   Result=true;
	   }
       return Result;
	}
    public static String getCurDate()
    {
    	SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
       return dateFormatter.format(System.currentTimeMillis());	
    }
    
    public static String getCurTime()
    {
    	SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
       return timeFormatter.format(System.currentTimeMillis());	
    }
    public static void  SplitString(String source,String Ch,List<String> Items)
	{
		try
        {
			Items.clear();
			boolean adjust=false;
			
			if ("".equalsIgnoreCase(source)) return ;
		      
			String  tSource=source;
			String  LastSub=source.substring(source.length()-Ch.length());
			if (LastSub.equalsIgnoreCase(Ch))
			{
				adjust=true;
				tSource=tSource+"P";
			}
			
			String []ItemInfo=tSource.split(Ch);
			if (ItemInfo!=null && !"".equalsIgnoreCase(source))
			{
				for (int i=0;i<ItemInfo.length;i++)
				{
				    Items.add(ItemInfo[i]);
				}
			}
			
			if (adjust)
			{
				Items.set(Items.size()-1,"");
			}
        }catch(Exception e)
        {
        }
		
	}
}
