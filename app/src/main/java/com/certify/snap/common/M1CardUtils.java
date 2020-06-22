package com.certify.snap.common;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**@author Burian
 * Created on 2020/3/10
 * @description MifareClassic卡片交易工具类
 */
public class M1CardUtils {
    /**
     * 错误类型：不支持MifareClassic
     */
    public static final String CARD_TYPE_ERROR = "100:Card type error";

    /**
     * 错误类型：密码验证错误
     */
    public static final String AUTHENTICATE_ERROR = "200:Verification failed";

    /**
     * 判断是否支持NFC
     * @return
     */
    public static NfcAdapter isNfcAble(Activity mContext){
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
        if (mNfcAdapter == null) {
           // Toast.makeText(mContext, "The device does not support NFC!", Toast.LENGTH_LONG).show();
        }
        else if (!mNfcAdapter.isEnabled()) {
//            Toast.makeText(mContext, "Please enable NFC function in system settings first!", Toast.LENGTH_LONG).show();
        }
        return mNfcAdapter;
    }

    /**
     * 读取卡片信息 使用默认的Key A 验证 轮询读取
     * @return
     */
    public static String[][] readCard(Tag tag) throws IOException {
        if(!isMifareClassic(tag)){
            throw new IOException(CARD_TYPE_ERROR);
        }

        MifareClassic mifareClassic = MifareClassic.get(tag);
        try {
            mifareClassic.connect();
            String[][] metaInfo = new String[16][4];
            // 获取TAG中包含的扇区数
            int sectorCount = mifareClassic.getSectorCount();
            for (int j = 0; j < sectorCount; j++) {
                int bCount;//当前扇区的块数
                int bIndex;//当前扇区第一块
                if (m1AuthWithKeyA(mifareClassic,j,MifareClassic.KEY_DEFAULT)) {
                    bCount = mifareClassic.getBlockCountInSector(j);
                    bIndex = mifareClassic.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mifareClassic.readBlock(bIndex);
                        String dataString = bytesToHexString(data);
                        metaInfo[j][i] = dataString;
                        Log.e("data :",dataString);
                        bIndex++;
                    }
                } else {
                    Log.e("readCard","password error");
                }
            }
            return metaInfo;
        } catch (IOException e){
            throw new IOException(e);
        } finally {
            try {
                mifareClassic.close();
            }catch (IOException e){
                throw new IOException(e);
            }
        }
    }

    /**
     * 读取数据 使用指定的Key A 验证
     * @param tag
     * @param blockIndex
     * @param key
     */
    public static int readBlockWithKeyA(Tag tag, int blockIndex , byte[] key) throws IOException {
        if(tag==null){
            throw new IOException();
        }
        if(!isMifareClassic(tag)){
            throw new IOException(CARD_TYPE_ERROR);
        }

        MifareClassic mifareClassic = MifareClassic.get(tag);
        try {
            mifareClassic.connect();
            if (m1AuthWithKeyA(mifareClassic,mifareClassic.blockToSector(blockIndex),key)) {
                return byteToInt(mifareClassic.readBlock(blockIndex));//todo byteToInt方法传入的是长度16的字节数组
            } else {
                throw new IOException(AUTHENTICATE_ERROR);
            }
        } catch (IOException e){
            throw new IOException(e);
        } finally {
            try {
                mifareClassic.close();
            }catch (IOException e){
                throw new IOException(e);
            }
        }
    }

    /**
     * 写入数据 使用指定的Key A 验证
     * @param blockIndex
     * @param value data
     */
    public static void writeBlockWithKeyA(Tag tag, int blockIndex, int value, byte[] key) throws IOException {
        if(tag==null || value < 0){
            throw new IOException();
        }
        if(!isMifareClassic(tag)){
            throw new IOException(CARD_TYPE_ERROR);
        }

        MifareClassic mifareClassic = MifareClassic.get(tag);
        try {
            mifareClassic.connect();
            if (m1AuthWithKeyA(mifareClassic,mifareClassic.blockToSector(blockIndex),key)) {
                mifareClassic.writeBlock(blockIndex, constructBuffer((byte)blockIndex,value));
                Log.e("writeBlock","success");
            } else {
                throw new IOException(AUTHENTICATE_ERROR);
            }
        } catch (IOException e){
            throw new IOException(e);
        } finally {
            try {
                mifareClassic.close();
            }catch (IOException e){
                throw new IOException(e);
            }
        }
    }

    /**
     * 修改密码 修改key A
     */
    public static void changeKeyA(Tag tag, int blockIndex, byte[] newKey,byte[] key) throws IOException {
        if(tag==null){
            throw new IOException();
        }
        if(!isMifareClassic(tag)){
            throw new IOException(CARD_TYPE_ERROR);
        }

        MifareClassic mifareClassic = MifareClassic.get(tag);
        try {
            mifareClassic.connect();
            if (m1AuthWithKeyA(mifareClassic,mifareClassic.blockToSector(blockIndex),key)) {
                byte[] oldKey = mifareClassic.readBlock(blockIndex);
                Log.e("old key",""+bytesToHexString(oldKey));
                System.arraycopy(newKey,0,oldKey,0,newKey.length);
                mifareClassic.writeBlock(blockIndex,oldKey);
                Log.e("new key",""+bytesToHexString(oldKey));
                Log.e("change key","success");
            } else {
                throw new IOException(AUTHENTICATE_ERROR);
            }
        } catch (IOException e){
            throw new IOException(e);
        } finally {
            try {
                mifareClassic.close();
            }catch (IOException e){
                throw new IOException(e);
            }
        }
    }

    /**
     * 数值增加 使用指定的Key A 验证
     * @param tag
     * @param blockIndex
     * @param value
     * @param key
     * @throws IOException
     */
    public static void incrementWithKeyA(Tag tag ,int blockIndex,int value,byte[] key) throws IOException{
        if(tag==null || value< 0){
            throw new IOException();
        }
        if(!isMifareClassic(tag)){
            throw new IOException(CARD_TYPE_ERROR);
        }

        MifareClassic mifareClassic = MifareClassic.get(tag);
        try {
            mifareClassic.connect();
            if (m1AuthWithKeyA(mifareClassic,mifareClassic.blockToSector(blockIndex),key)) {
//                mifareClassic.increment(blockIndex,value);
                int amount = byteToInt(mifareClassic.readBlock(blockIndex)) + value;
                mifareClassic.writeBlock(blockIndex,constructBuffer((byte)blockIndex,amount));
                Log.e("increment","success");
            } else {
                throw new IOException(AUTHENTICATE_ERROR);
            }
        } catch (IOException e){
            throw new IOException(e);
        } finally {
            try {
                mifareClassic.close();
            }catch (IOException e){
                throw new IOException(e);
            }
        }
    }

    /**
     * 数值减少 使用指定的Key A 验证 结果数据不能小于0
     * @param tag
     * @param blockIndex
     * @param value
     * @param key
     * @throws IOException
     */
    public static void decrementWithKeyA(Tag tag ,int blockIndex,int value,byte[] key) throws IOException{
        if(tag==null || value < 0){
            throw new IOException();
        }
        if(!isMifareClassic(tag)){
            throw new IOException(CARD_TYPE_ERROR);
        }

        MifareClassic mifareClassic = MifareClassic.get(tag);
        try {
            mifareClassic.connect();
            if (m1AuthWithKeyA(mifareClassic,mifareClassic.blockToSector(blockIndex),key)) {
//                mifareClassic.decrement(blockIndex,value);
                int amount = byteToInt(mifareClassic.readBlock(blockIndex)) - value;
                if(amount >= 0) mifareClassic.writeBlock(blockIndex,constructBuffer((byte)blockIndex,amount));
                else throw new IOException();
                Log.e("decrement","success");
            } else {
                throw new IOException(AUTHENTICATE_ERROR);
            }
        } catch (IOException e){
            throw new IOException(e);
        } finally {
            try {
                mifareClassic.close();
            }catch (IOException e){
                throw new IOException(e);
            }
        }
    }

    /**
     * 是否支持MifareClassic
     * @param tag
     * @return
     */
    private static boolean isMifareClassic(Tag tag){
        if(tag==null){
            return false;
        }
        String[] techList = tag.getTechList();
        boolean haveMifareClassic = false;
        for (String tech : techList) {
            if (tech.contains("MifareClassic")) {
                haveMifareClassic = true;
                break;
            }
        }
        return haveMifareClassic;
    }

    /**
     * 密码校验
     * @param mTag
     * @param position sector
     * @param key Specified password
     * @return authenticate result of Key A
     * @throws IOException
     */
    private static boolean m1AuthWithKeyA(MifareClassic mTag,int position,byte[] key) throws IOException {
        if(mTag != null && key!=null) {
            Log.e("auth key a","start__" + position + "  key :"+ bytesToHexString(key));
            return mTag.authenticateSectorWithKeyA(position, key);
        }
        return false;
    }

    /**
     * 密码校验
     * @param mTag
     * @param position sector
     * @param key Specified password
     * @return authenticate result of Key B
     * @throws IOException
     */
    private static boolean m1AuthWithKeyB(MifareClassic mTag,int position,byte[] key) throws IOException {
        if(mTag != null && key!=null) {
            Log.e("auth key a","start__" + position + "  key :"+ bytesToHexString(key));
            return mTag.authenticateSectorWithKeyB(position, key);
        }
        return false;
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    /**
     * 整数转为长度16的byte数组
     * @param blockNo
     * @param value
     * @return
     */
    public static byte[] constructBuffer(byte blockNo, int value) {
        byte[] result = new byte[16];
        for (int i = 0; i < 4; i++) {
            result[i] = (byte) (value % 0x100);
            value /= 0x100;
        }
        // 2nd and 3rd 4 bytes
        for (int i = 0; i < 4; i++) {
            result[4 + i] = (byte) ~result[i];
            result[8 + i] = result[i];
        }

        // last 4 bytes
        result[12] = blockNo;
        result[13] = (byte) ~blockNo;
        result[14] = blockNo;
        result[15] = (byte) ~blockNo;
        return result;
    }

    public static int byteToInt(byte[] value) {
        int result = 0;
        for (int i = 3; i >= 0; i--) {
            result = result * 0x100 + (value[i] & 0xff);
        }
        return result;
    }
}
