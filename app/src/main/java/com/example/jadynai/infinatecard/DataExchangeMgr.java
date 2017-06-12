package com.example.jadynai.infinatecard;

/**
 * @version:
 * @FileDescription:
 * @Author:jing
 * @Since:2017/6/12
 * @ChangeList:
 */

public class DataExchangeMgr {

    private static volatile DataExchangeMgr singleton = null;

    private DataExchangeMgr() {
    }

    private Integer mCurrentData;

    private Integer mOrignalData;

    public static DataExchangeMgr getInstance() {
        if (singleton == null) {
            synchronized (DataExchangeMgr.class) {
                if (singleton == null) {
                    singleton = new DataExchangeMgr();
                }
            }
        }
        return singleton;
    }

    public void saveCurrData(Integer data) {
        mCurrentData = data;
    }

    public void saveOrignData(Integer orignData) {
        mOrignalData = orignData;
    }

    public Integer getCurrentData() {
        return mCurrentData;
    }

    public Integer getOrignalData() {
        return mOrignalData;
    }
}
