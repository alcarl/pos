package cn.basewin.unionpay.network;

import android.text.TextUtils;
import android.util.Log;

import com.basewin.packet8583.factory.Iso8583Manager;
import com.basewin.utils.BytesUtil;

import java.util.Map;

import cn.basewin.unionpay.ActionConstant;
import cn.basewin.unionpay.entity.Card;
import cn.basewin.unionpay.network.remote.AnnotationNet;
import cn.basewin.unionpay.network.remote.ReverseTradeMessage;
import cn.basewin.unionpay.setting.SettingConstant;
import cn.basewin.unionpay.trade.FlowControl;
import cn.basewin.unionpay.trade.InputMoneyAty;
import cn.basewin.unionpay.utils.BCDHelper;
import cn.basewin.unionpay.utils.PosUtil;
import cn.basewin.unionpay.utils.TradeEncUtil;

/**
 * 作   者：hanlei
 * 创建时间：2016/8/17 12:21
 * 描   述：电子现金圈存（现金）
 */
@AnnotationNet(action = ActionConstant.ACTION_ECLOAD_CASH)
public class NetECLoadCash extends ReverseTradeMessage {
    private static final String TAG = NetECLoadCash.class.getName();

    @Override
    protected Iso8583Manager getEncryption(Map<String, Object> map, Iso8583Manager iso) throws Exception {
        Card card = FlowControl.MapHelper.getCard();
        iso.setBit("msgid", "0200");
        iso.setBit(3, "630000");
        String s = PosUtil.yuanTo12((String) map.get(InputMoneyAty.KEY_MONEY));
        iso.setBit(4, s);

        String _22 = PosUtil._22(card);
        iso.setBit(22, _22);

        iso.setBit(25, "91");

        if (card != null) {
            iso.setBit(14, card.getExpDate());
            if (!card.isIC()) {
                if (!TextUtils.isEmpty(card.getTrack3ToD())) {
                    iso.setBit(36, card.getTrack3ToD());
                }
            } else {
                iso.setBit(23, card.get23());
                iso.setBinaryBit(55, BCDHelper.stringToBcd(card.get55()));
            }
        }

        if (card != null) {
            String track2 = card.getTrack2ToD();
            iso.setBit(35, track2);
        }
        iso.setBit(49, "156");
        String batchAuto = SettingConstant.getBatch();
        FlowControl.MapHelper.setBatch(batchAuto);
        iso.setBit(60, "46" + batchAuto + "000" + "6" + "0");
        iso.setBit(64, "0000000000000000");
        byte[] macInput = iso.getMacData("msgid", "63");
        String mac = TradeEncUtil.getMacECB(macInput);
        Log.d(TAG, "macInput=" + BytesUtil.bytes2HexString(macInput));
        Log.d(TAG, "macInput.length=" + macInput.length);
        Log.d(TAG, "mac=" + mac);
        iso.setBinaryBit(64, BCDHelper.stringToBcd(mac));
        return iso;
    }
}
