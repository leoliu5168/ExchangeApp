# 匯率轉換（Android）

多國貨幣即時換算＋計算機。Kotlin + Jetpack Compose，深色精緻 UI。

## 功能
- 多列貨幣同步換算；點任一列金額即切換成輸入列（橘色外框）
- 計算機：`+ − × ÷ %`、倒退（長按清除）、`C`；`100 + 10%` = 110
- 點每列左側國旗（平面國旗圖示）可更換貨幣（選到已存在的貨幣會自動互換位置）
- 右上「管理貨幣」：新增、移除、拖曳排序
- 鍵盤上方快捷列：
  - **免稅額**：直接帶入該國免稅最低消費（例：日本 ¥5,000）
  - **退稅額**：以目前含稅金額估算退稅（例：日本 ×10÷110），未達門檻會提示
  - **9折／88折／85折**，以及在「設定」新增的自訂折扣
  - 國家預設自動判斷（目前輸入的外幣或列表中第一個外幣），可在設定中指定；支援日、韓、泰、新、澳、中、越、台、法、德、義、西、荷、瑞士
- 上方提示訊息：可左右或往上滑掉；設定中可選「5 秒後自動消失」或「保持顯示直到滑掉」
- 右上「紀錄」：保留最近 20 筆計算（按 `=` 時儲存），點一下帶回
- 匯率：開啟 App 時立即抓取，前景期間每 3 分鐘自動更新；背景時暫停省電；離線時沿用快取並標示

## 匯率來源（免費、免金鑰）
1. [fawazahmed0/exchange-api](https://github.com/fawazahmed0/exchange-api)（開源，CC0）— jsDelivr CDN
2. 同資料的 Cloudflare Pages 鏡像
3. [open.er-api.com](https://www.exchangerate-api.com/docs/free)（備援）

> 註：免費來源的匯率約每日更新一次，App 每 3 分鐘會重新檢查，但數字不一定每次都會變動。

## 取得 APK
每次推送到此資料夾，GitHub Actions 會自動建置：
- Releases → **fxcalc-latest** → 下載 `FxCalc.apk`
- 或 Actions → 最新一次「Build FX Calculator APK」→ Artifacts

## 本機建置
需要 Android SDK（或 Android Studio）：
```bash
cd currency-converter-android
./gradlew assembleRelease
```

國旗圖片來自 [flag-icons](https://github.com/lipis/flag-icons)（MIT 授權，見 `FLAGS_LICENSE.txt`）。
