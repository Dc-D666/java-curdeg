package cn.edu.sdu.java.server.payload.request;


import cn.edu.sdu.java.server.util.DateTimeTool;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class DataRequest {
    private Map<String,Object> data;
    
    private Integer pageNum;
    private Integer pageSize;

    public DataRequest() {
        data = new HashMap<>();
    }

    @com.fasterxml.jackson.annotation.JsonAnySetter
    public void setUnknownField(String name, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(name, value);
    }

    @com.fasterxml.jackson.annotation.JsonAnyGetter
    public Map<String, Object> getUnknownFields() {
        return data != null ? data : new HashMap<>();
    }

    @com.fasterxml.jackson.annotation.JsonSetter("data")
    public void setDataField(Map<String, Object> dataField) {
        if (dataField != null) {
            if (this.data == null) {
                this.data = new HashMap<>();
            }
            this.data.putAll(dataField);
        }
    }

    public void add(String key, Object obj){
        data.put(key,obj);
    }
    public Object get(String key){
        return data.get(key);
    }

    public String getString(String key){
        Object obj = data.get(key);
        if(obj == null)
            return null;
        if(obj instanceof String)
            return (String)obj;
        return obj.toString();
    }
    public Boolean getBoolean(String key){
        Object obj = data.get(key);
        if(obj == null)
            return false;
        if(obj instanceof Boolean)
            return (Boolean)obj;
        return "true".equals(obj.toString());
    }

    public List<?> getList(String key){
        Object obj = data.get(key);
        if(obj == null)
            return new ArrayList<>();
        if(obj instanceof List)
            return (List<?>)obj;
        else
            return new ArrayList<>();
    }
    public Map<String,Object> getMap(String key){
        if(data == null)
            return new HashMap<>();
        Object obj = data.get(key);
        if(obj == null)
            return new HashMap<>();
        if(obj instanceof Map)
            return (Map<String,Object>)obj;
        else
            return new HashMap<>();
    }

    public Integer getInteger(String key) {
        if ("pageNum".equals(key) && pageNum != null) {
            return pageNum;
        }
        if ("pageSize".equals(key) && pageSize != null) {
            return pageSize;
        }
        
        if(data == null)
            return null;
        Object obj = data.get(key);
        if(obj == null)
            return null;
        if(obj instanceof Integer)
            return (Integer)obj;
        if(obj instanceof Double)
            return ((Double)obj).intValue();
        if(obj instanceof Long)
            return ((Long)obj).intValue();
        return null;
    }

    public Long getLong(String key){
        if(data == null)
            return null;
        Object obj = data.get(key);
        if(obj == null)
            return null;
        if(obj instanceof Long)
            return (Long)obj;
        if(obj instanceof Integer)
            return ((Integer)obj).longValue();
        if(obj instanceof Double)
            return ((Double)obj).longValue();
        return null;
    }

    public Double getDouble(String key){
        if(data == null)
            return null;
        Object obj = data.get(key);
        if(obj == null)
            return null;
        if(obj instanceof Double)
            return (Double)obj;
        if(obj instanceof Integer)
            return ((Integer)obj).doubleValue();
        if(obj instanceof Long)
            return ((Long)obj).doubleValue();
        return null;
    }

    public Date getDate(String key){
        if(data == null)
            return null;
        Object obj = data.get(key);
        if(obj == null)
            return null;
        if(obj instanceof Date)
            return (Date)obj;
        if(obj instanceof String){
            return DateTimeTool.formatDateTime((String)obj, "yyyy-MM-dd HH:mm:ss");
        }
        return null;
    }

    public void setInteger(String key, Integer value){
        if(data != null){
            data.put(key,value);
        }
    }

    public void setString(String key, String value){
        if(data != null){
            data.put(key,value);
        }
    }

    public void setLong(String key, Long value){
        if(data != null){
            data.put(key,value);
        }
    }

    public void setDouble(String key, Double value){
        if(data != null){
            data.put(key,value);
        }
    }

    public void setDate(String key, Date value){
        if(data != null){
            data.put(key,value);
        }
    }

    public boolean contains(String key){
        if(data == null)
            return false;
        return data.containsKey(key);
    }

    public void remove(String key){
        if(data != null){
            data.remove(key);
        }
    }

    public int size(){
        if(data == null)
            return 0;
        return data.size();
    }

    public Set<String> keySet(){
        if(data == null)
            return new HashSet<>();
        return data.keySet();
    }

    public boolean isEmpty(){
        if(data == null)
            return true;
        return data.isEmpty();
    }

    public Integer getCurrentPage() {
        Integer page = getInteger("currentPage");
        if (page == null || page < 0) {
            return 0;
        }
        return page;
    }
}
