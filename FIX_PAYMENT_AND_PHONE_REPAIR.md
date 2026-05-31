# 支付方式与手机号显示修复文档

## 修复内容

1. 订单金额计算
   - 修改 `StudentOrderController.submitOrder`。
   - 下单时使用菜品 `originalPrice` 计算订单总金额。
   - 使用菜品当前 `price` 计算实付金额。
   - 新增优惠金额计算：`discountAmount = totalAmount - actualAmount`。

2. 支付方式保存
   - `SubmitOrderDTO` 新增 `payMethod` 字段，支持下单时传入支付方式。
   - `StudentOrderController.payOrder` 支持从请求参数或 JSON 请求体读取 `payMethod`。
   - 学生支付成功后更新 `orders.pay_method`，商家端不再显示“未支付”。
   - 未传支付方式时默认保存为“微信支付”。

3. 学生手机号接口与显示
   - 新增 `/api/student/profile` 接口，返回当前登录学生的用户名、真实姓名、手机号、头像等信息。
   - 学生主页登录后调用该接口，将姓名和手机号显示在顶部。
   - 学生个人中心调用该接口，显示真实手机号。
   - 学生订单详情接口返回 `userName` 和 `phone`，订单详情页优先显示后端返回的手机号。

4. 订单详情字段补全
   - 学生订单详情接口补充返回 `discountAmount`、`payMethod`、`cancelReason`、`userId`、`userName`、`phone`。
   - 学生订单详情页显示真实支付方式、订单总金额、优惠金额、实付金额和联系电话。

5. 商家端订单详情金额显示
   - 修改 `front/src/views/Merchant/Order.vue`。
   - 将订单详情弹窗中的“订单总金额”和“实付金额”显示位置调换为正确顺序。
   - 保留优惠金额、支付方式、联系方式显示。

6. 管理员端拉黑商家无反应
   - 定位原因：数据库表 `merchant_blacklist.user_id` 为 `NOT NULL`，且外键关联学生表；但管理员端拉黑的是整家商家，只写入 `merchant_id` 和 `reason`，导致插入黑名单失败。
   - 修改 `DataInitializer`，项目启动时自动检测并修复 `merchant_blacklist.user_id` 字段，将其调整为允许为空，支持“商家级黑名单”。
   - 已同步修复当前本地数据库结构：`merchant_blacklist.user_id` 允许为空。
   - 修改 `front/src/views/Admin/MerchantList.vue`，给“确认拉黑”请求增加异常捕获，接口失败时会弹出错误信息，不再表现为点击无反应。
   - 重新构建前端并同步到 `src/main/resources/static/index.html`，确保通过 Spring Boot 静态资源访问管理员端时加载的是最新页面。

## 涉及文件

- `src/main/java/com/campus/controller/student/StudentOrderController.java`
- `src/main/java/com/campus/controller/student/StudentMerchantController.java`
- `src/main/java/com/campus/dto/student/SubmitOrderDTO.java`
- `src/main/resources/static/student_home.html`
- `src/main/resources/static/student_profile.html`
- `src/main/resources/static/student_orders.html`
- `src/main/resources/static/student_order_detail.html`
- `src/main/java/com/campus/common/config/DataInitializer.java`
- `front/src/views/Admin/MerchantList.vue`
- `front/src/views/Merchant/Order.vue`
- `src/main/resources/static/index.html`

## 验证建议

1. 学生登录后进入主页，确认顶部显示姓名和手机号。
2. 学生下单后进入订单列表，点击支付，输入支付方式并完成支付。
3. 商家端查看该订单，确认支付方式显示为学生选择的支付方式。
4. 学生进入订单详情页，确认联系电话、支付方式、订单总金额、优惠金额、实付金额显示正确。
5. 管理员进入商家管理，点击“拉黑”，填写管控原因后确认，确认商家状态变为“禁止营业”，黑名单管理中出现该商家记录。
