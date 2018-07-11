package com.mofangyouxuan.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;



/**
 * HTTP 请求工具类
 * @author Jee Khan
 *
 */
public class HttpUtils {
    private static RequestConfig requestConfig;	
    //private static String TEMP_FILE_DIR = "";			//文件临时保存目录
    private static final int MAX_TIMEOUT = 7000;	//连接超时

    static {
        RequestConfig.Builder configBuilder = RequestConfig.custom();
        // 设置连接超时
        configBuilder.setConnectTimeout(MAX_TIMEOUT);
        // 设置读取超时
        configBuilder.setSocketTimeout(MAX_TIMEOUT);
        // 设置从连接池获取连接实例的超时
        configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);

        requestConfig = configBuilder.build();
    }

    /**
     * 发送 GET 请求（HTTP），不带参数
     * @param url
     * @return
     * @throws IOException 
     */
    public static String doGet(String url) {
        return doGet(url, new HashMap<String, Object>());
    }

    /**
     * 发送 GET 请求（HTTP），K-V形式
     * @param url
     * @param params
     * @return
     * @throws IOException 
     */
    public static String doGet(String url, Map<String, Object> params) {
        String apiUrl = url;
        StringBuffer param = new StringBuffer();
        int i = 0;
        for (String key : params.keySet()) {
            if (i == 0)
                param.append("?");
            else
                param.append("&");
            param.append(key).append("=").append(params.get(key));
            i++;
        }
        apiUrl += param;
        String result = null;
        
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(apiUrl);
        CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                try {
                	result = IOUtils.toString(instream, "UTF-8");
                } finally {
                    instream.close();
                }
            }
        } catch (IOException e) {
			e.printStackTrace();
		} finally {
            try {
				response.close();
			} catch (IOException e) {
			}
            try {
				httpclient.close();
			} catch (IOException e) {
			}
        }
        
        return result;
    }

    /**
     * 发送 GET 请求（HTTP），K-V形式
     * @param url
     * @param params
     * @return
     * @throws IOException 
     */
    public static String doGet(String url, Map<String, String> headers,Map<String, Object> params) {
        String apiUrl = url;
        StringBuffer param = new StringBuffer();
        int i = 0;
        for (String key : params.keySet()) {
            if (i == 0)
                param.append("?");
            else
                param.append("&");
            param.append(key).append("=").append(params.get(key));
            i++;
        }
        apiUrl += param;
        String result = null;
        
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(apiUrl);
        for(Map.Entry<String, String> entry:headers.entrySet()) {
        		httpget.addHeader(entry.getKey(), entry.getValue());
        }
        CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                try {
                	result = IOUtils.toString(instream, "UTF-8");
                } finally {
                    instream.close();
                }
            }
        } catch (IOException e) {
			e.printStackTrace();
		} finally {
            try {
				response.close();
			} catch (IOException e) {
			}
            try {
				httpclient.close();
			} catch (IOException e) {
			}
        }
        
        return result;
    }
    
    /**
     * 发送 POST 请求（HTTP），不带输入数据
     * @param apiUrl
     * @return
     */
    public static String doPost(String apiUrl) {
        return doPost(apiUrl, new HashMap<String, Object>());
    }

    /**
     * 发送 POST 请求（HTTP），K-V形式
     * @param apiUrl API接口URL
     * @param params 参数map
     * @return
     */
    public static String doPost(String apiUrl, Map<String, Object> params) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String httpStr = null;
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;

        try {
            httpPost.setConfig(requestConfig);
            List<NameValuePair> pairList = new ArrayList<>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue()== null ? "" :entry.getValue().toString());
                pairList.add(pair);
            }
            httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("UTF-8")));
            response = httpClient.execute(httpPost);
            System.out.println(response.toString());
            HttpEntity entity = response.getEntity();
            httpStr = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return httpStr;
    }

    /**
     * 发送 POST 请求（HTTP），JSON形式
     * @param apiUrl
     * @param json json对象
     * @return
     */
    public static String doPost(String apiUrl, String json) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String httpStr = null;
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;

        try {
            httpPost.setConfig(requestConfig);
            StringEntity stringEntity = new StringEntity(json,"UTF-8");//解决中文乱码问题
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            System.out.println(response.getStatusLine().getStatusCode());
            httpStr = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return httpStr;
    }

    /**
     * 发送  SSL GET 请求（HTTP）
     * @param url
     * @param params
     * @return
     */
    public static String doGetSSL(String url) {
    	return doGetSSL(url,new HashMap<String, Object>());
    }
    
    /**
     * 发送  SSL GET 请求（HTTP），K-V形式
     * @param url
     * @param params
     * @return
     */
    public static String doGetSSL(String url, Map<String, Object> params) {
        String apiUrl = url;
        StringBuffer param = new StringBuffer();
        int i = 0;
        for (String key : params.keySet()) {
            if (i == 0)
                param.append("?");
            else
                param.append("&");
            param.append(key).append("=").append(params.get(key));
            i++;
        }
        apiUrl += param;
        String result = null;
        CloseableHttpClient httpClient = createSSLConnSocketFactory();
        HttpGet httpGet = new HttpGet(apiUrl);
        CloseableHttpResponse response = null;
        try {
        	httpGet.setConfig(requestConfig);
            response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            result = EntityUtils.toString(entity, "utf-8");
        } catch (Exception e) {
			e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return result;
    }
    
    /**
     * 发送  SSL GET 请求（HTTP），K-V形式
     * @param url
     * @param params
     * @return
     */
    public static String doGetSSL(String url, Map<String, String> headers,Map<String, Object> params) {
        String apiUrl = url;
        StringBuffer param = new StringBuffer();
        int i = 0;
        for (String key : params.keySet()) {
            if (i == 0)
                param.append("?");
            else
                param.append("&");
            param.append(key).append("=").append(params.get(key));
            i++;
        }
        apiUrl += param;
        String result = null;
        CloseableHttpClient httpClient = createSSLConnSocketFactory();
        HttpGet httpGet = new HttpGet(apiUrl);
        for(Map.Entry<String, String> entry:headers.entrySet()) {
        		httpGet.addHeader(entry.getKey(), entry.getValue());
        }
        CloseableHttpResponse response = null;
        try {
        	httpGet.setConfig(requestConfig);
            response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            result = EntityUtils.toString(entity, "utf-8");
        } catch (Exception e) {
			e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return result;
    }
    /**
     * 发送 SSL POST 请求（HTTPS），K-V形式
     * @param apiUrl API接口URL
     * @param params 参数map
     * @return
     */
    public static String doPostSSL(String apiUrl, Map<String, Object> params) {
        CloseableHttpClient httpClient = createSSLConnSocketFactory();
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;
        String httpStr = null;

        try {
            httpPost.setConfig(requestConfig);
            List<NameValuePair> pairList = new ArrayList<NameValuePair>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry
                        .getValue().toString());
                pairList.add(pair);
            }
            httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("utf-8")));
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            httpStr = EntityUtils.toString(entity, "utf-8");
        } catch (Exception e) {
			e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return httpStr;
    }

    /**
     * 发送 SSL POST 请求（HTTPS），JSON形式
     * @param apiUrl API接口URL
     * @param json JSON对象
     * @return
     * @throws IOException 
     */
    public static String doPostSSL(String apiUrl, String json) {
        CloseableHttpClient httpClient = createSSLConnSocketFactory();
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;
        String httpStr = null;

        try {
            httpPost.setConfig(requestConfig);
            StringEntity stringEntity = new StringEntity(json,"UTF-8");//解决中文乱码问题
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            httpStr = EntityUtils.toString(entity, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return httpStr;
    }

    /**
     * 发送 SSL POST 请求（HTTPS）
     * @param apiUrl API接口URL
     * @param content 发送内容
     * @return
     * @throws IOException 
     */
    public static String doPostTextSSL(String apiUrl, String content) {
        CloseableHttpClient httpClient = createSSLConnSocketFactory();
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;
        String httpStr = null;

        try {
            httpPost.setConfig(requestConfig);
            StringEntity stringEntity = new StringEntity(content,"UTF-8");//解决中文乱码问题
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("text/plain");
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            httpStr = EntityUtils.toString(entity, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return httpStr;
    }

    /**
     * 发送 SSL POST 请求（HTTPS）
     * @param apiUrl API接口URL
     * @param content 发送内容
     * @return
     * @throws IOException 
     */
    public static String doPostTextSSL(CloseableHttpClient httpClient,String apiUrl, String content) {
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;
        String httpStr = null;

        try {
            httpPost.setConfig(requestConfig);
            StringEntity stringEntity = new StringEntity(content,"UTF-8");//解决中文乱码问题
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("text/plain");
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            httpStr = EntityUtils.toString(entity, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return httpStr;
    }
    
    /**
     * 创建SSL安全连接
     *
     * @return
     */
    private static CloseableHttpClient createSSLConnSocketFactory()  {
	    	SSLContext sslContext = SSLContexts.createSystemDefault();
	    	SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,NoopHostnameVerifier.INSTANCE);//不验证主机
	
	    	ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
	    	
	    	Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
	    	        .register("http", plainsf)
	    	        .register("https", sslsf)
	    	        .build();
	
	    	HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(r);
	    	CloseableHttpClient client = HttpClients.custom().setConnectionManager(cm).build();
	    	return client;
    }


    /**
     * SSL 文件上传（POST SSL）
     * @param apiUrl	文件上传URL
     * @param file		需要上传的文件
     * @param fileField	Form表单中file的名称
     * @param content_type	文件MIME类型
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
	public static String uploadFileSSL(String apiUrl, File file,String fileField,Map<String,String> paramPairs) {
		CloseableHttpClient httpClient = createSSLConnSocketFactory();
		CloseableHttpResponse httpResponse = null;
		HttpPost httpPost = new HttpPost(apiUrl);
		String httpStr;
		try {
	        
			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
			FileBody fileBody = new FileBody(file);
			entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);// 以浏览器兼容模式运行，防止文件名乱码。
			entityBuilder.addPart(fileField, fileBody);	//对应服务端类的同名属性<File类型>
			entityBuilder.setCharset(CharsetUtils.get("UTF-8"));
			//处理文字字段：放入字段名，字段值，以及contentType
			ContentType strContent=ContentType.create("text/plain",Charset.forName("UTF-8"));
			if(paramPairs != null && paramPairs.size()>0){
				for (Map.Entry<String, String> entry : paramPairs.entrySet()) {
	                //entityBuilder.addPart(entry.getKey(), new StringBody(entry.getValue(),ContentType.DEFAULT_TEXT));
					entityBuilder.addTextBody(entry.getKey(), entry.getValue(),strContent);
	            }
			}
			HttpEntity reqEntity = entityBuilder.build();
			httpPost.setConfig(requestConfig);
			httpPost.setEntity(reqEntity);
			httpResponse = httpClient.execute(httpPost);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            HttpEntity entity = httpResponse.getEntity();
            if (entity == null) {
                return null;
            }
            httpStr = EntityUtils.toString(entity, "utf-8");
            return httpStr;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (Exception e) {
				}
			}
			try {
				httpClient.close();
			} catch (IOException e) {
			}
		}
		return null;
	}

    /**
     * 文件上传（POST）
     * @param apiUrl	文件上传URL
     * @param file		需要上传的文件
     * @param fileField	Form表单中file的名称 
     * @param content_type	文件MIME类型
     * @return
     */
	public static String uploadFile(String apiUrl, File file,String fileField,Map<String,String> paramPairs) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse httpResponse = null;
		HttpPost httpPost = new HttpPost(apiUrl);
		String httpStr;
		try {
	        
			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
			FileBody fileBody = new FileBody(file);
			entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);	// 以浏览器兼容模式运行，防止文件名乱码。
			entityBuilder.addPart(fileField, fileBody);	//对应服务端类的同名属性<File类型>
			entityBuilder.setCharset(CharsetUtils.get("UTF-8"));
			//处理文字字段：放入字段名，字段值，以及contentType
			ContentType strContent=ContentType.create("text/plain",Charset.forName("UTF-8"));
			if(paramPairs != null && paramPairs.size()>0){
				for (Map.Entry<String, String> entry : paramPairs.entrySet()) {
	                //entityBuilder.addPart(entry.getKey(), new StringBody(entry.getValue(),ContentType.DEFAULT_TEXT));
					entityBuilder.addTextBody(entry.getKey(), entry.getValue(),strContent);
	            }
			}
			HttpEntity reqEntity = entityBuilder.build();
			httpPost.setConfig(requestConfig);
			httpPost.setEntity(reqEntity);
			httpResponse = httpClient.execute(httpPost);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            HttpEntity entity = httpResponse.getEntity();
            if (entity == null) {
                return null;
            }
            httpStr = EntityUtils.toString(entity, "utf-8");
            return httpStr;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (Exception e) {
				}
			}
			try {
				httpClient.close();
			} catch (IOException e) {
			}
		}
		return null;
	}
	
	/**
	 * 文件下载（GET）
	 * @param fileSaveDir 文件本地保存目录
	 * @param apiUrl	文件下载路径
	 * @return
	 */
	public static File downloadFile(String fileSaveDir,String apiUrl){
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse httpResponse = null;
		HttpGet httpGet = new HttpGet(apiUrl);
		try {
			httpGet.setConfig(requestConfig);
			httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            HttpEntity entity = httpResponse.getEntity();
            if (entity == null) {
                return null;
            }
            InputStream in = entity.getContent();
            Header fileHead= httpResponse.getFirstHeader("filename");
            String typeHead = entity.getContentType().getValue();
            String type = typeHead.substring(typeHead.lastIndexOf("/"));
            String fileName = UUID.randomUUID().toString() + "." +type;
            if(fileHead != null){
            		fileName = fileHead.getValue();
            }
            File file = new File(fileSaveDir + fileName);
            FileUtils.copyInputStreamToFile(in, file);
            return file;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (Exception e) {
				}
			}
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 *  SSL 文件下载（GET）
	 * @param fileSaveDir 文件本地保存目录
	 * @param apiUrl	文件下载路径
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static File downloadFileSSL(String fileSaveDir,String apiUrl){
		CloseableHttpClient httpClient = createSSLConnSocketFactory();
		CloseableHttpResponse httpResponse = null;
		HttpGet httpGet = new HttpGet(apiUrl);
		try {
			httpGet.setConfig(requestConfig);
			httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            HttpEntity entity = httpResponse.getEntity();
            if (entity == null) {
                return null;
            }
            InputStream in = entity.getContent();
            Header fileHead= httpResponse.getFirstHeader("filename");
            String typeHead = entity.getContentType().getValue();
            String type = "tmp";
            if(typeHead != null) {
            		if(typeHead.contains("text/plain")) {
            			type = "txt";
            		}else {
            			if(typeHead.contains(";")) {
            				typeHead = typeHead.substring(typeHead.indexOf(";"));
            			}
            			if(typeHead.contains("/")) {
            				type = typeHead.substring(typeHead.lastIndexOf("/")+1);
            			}
            		}
            }
            String fileName = UUID.randomUUID().toString() + "." +type;
            if(fileHead != null){
            		fileName = fileHead.getValue();
            }else {
            		fileHead = httpResponse.getFirstHeader("Content-Disposition");
            		if(fileHead != null) {
            			if(fileHead.getValue() != null) {
            				String fname = fileHead.getValue();
            				if(fname.contains("filename=")) {
            					int index = fname.indexOf("filename=") + "filename=".length();
            					fname = fname.substring(index);
            					fileName = fname;
            				}
            			}
            		}
            }
            File file = new File(fileSaveDir + fileName);
            FileUtils.copyInputStreamToFile(in, file);
            return file;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (Exception e) {
				}
			}
			try {
				httpClient.close();
			} catch (IOException e) {
			}
		}
		return null;
	}
	
	/**
     * 下载文件（SSL POST），JSON形式参数
     * @param fileSaveDir 文件本地保存目录
     * @param apiUrl API接口URL
     * @param contentType	文本类型：text/plain、application/json;
     * @param params xml、json、txt等字符串，具体格式由contentType确定
     * @return
     * @throws IOException 
     */
    public static File downloadFileSSL(String fileSaveDir,String apiUrl, String contentType,String params) {
        CloseableHttpClient httpClient = createSSLConnSocketFactory();
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;
        try {
            httpPost.setConfig(requestConfig);
            StringEntity stringEntity = new StringEntity(params,"UTF-8");//解决中文乱码问题
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType(contentType);
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            InputStream in = entity.getContent();
            Header fileHead= response.getFirstHeader("filename");
            String typeHead = entity.getContentType().getValue();
            String type = "tmp";
            if(typeHead != null) {
            		if(typeHead.contains("text/plain")) {
            			type = "txt";
            		}else {
            			if(typeHead.contains(";")) {
            				typeHead = typeHead.substring(typeHead.indexOf(";"));
            			}
            			if(typeHead.contains("/")) {
            				type = typeHead.substring(typeHead.lastIndexOf("/")+1);
            			}
            		}
            }
            String fileName = UUID.randomUUID().toString() + "." +type;
            if(fileHead != null){
            		fileName = fileHead.getValue();
            }else {
            		fileHead = response.getFirstHeader("Content-Disposition");
            		if(fileHead != null) {
            			if(fileHead.getValue() != null) {
            				String fname = fileHead.getValue();
            				if(fname.contains("filename=")) {
            					int index = fname.indexOf("filename=") + "filename=".length();
            					fname = fname.substring(index);
            					fileName = fname;
            				}
            			}
            		}
            }
            File file = new File(fileSaveDir + fileName);
            FileUtils.copyInputStreamToFile(in, file);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return null;
    }
    
}
