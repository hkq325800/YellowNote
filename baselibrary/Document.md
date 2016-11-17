##命名规范
>例子以——表示
着重内容以【】表示
可替代内容以()表示

1. layout文件的命名
全小写，以下划线分割单词
```
activity_(场景名称)——activity_address_edit
fragment_(场景名称)_(作用)——fragment_address_map
列表项：item_(列表名称)_(位置)——item_folder_top
可复用layout：include_(作用)——include_navigation
```

2. dimens中的命名
【调换位置】并以text_size、height、width、margin开头方便分类寻找，再在后面加上【所代表的数值】一目了然
```
large_text_size——text_size_large_16sp
head_text_height——length_head_text_25dp
tool_margins——margin_tool_5dp
```
3. layout中id的命名
"m"+(场景名称)+(控件类型缩写，见附录1)——mLoginBtn，activity/fragment/item等具体文件中与layout中的id保持一致，方便查找和替换，即
```
@BindView(R.id.mLoginBtn)
Button mLoginBtn;
```
4. attr中的命名
在最前添加【与widget相关的】特异性前缀以避免重复

5. style和string中的注意点
每个场景类别中的内容【分离整理】，以
```
<!--xlistview-->
```
的形式描述并放在每个场景类别的【第一个内容前】

6. Java文件的命名、文件夹的命名
文件夹都用小写字母
文件命名每个单词首字母大写
Activity、Fragment、Adapter、Service、Broadcast、Provider、Listener、Event、Utils等关键文件以这些单词【结尾】，如LoginActivity。
在Activity和Fragment开头以
```
/**
*病例详情
*……
**/
```
的形式【描述界面文件】的标题
Model文件命名【通过接口名进行适当联想】，以Model结尾
而如自定义Widget等自定义类不受该限制，【看到名字知道是什么内容】即可

7. 有针对性地使用MVP形式

8. build.gradle中compile的dependency及apply的plugin说明用途

9. drawable里的selector用selector开头，drawable放置selector类型的代码，drawable-hdpi~drawable-xxhdpi放对应尺寸的文件

附录1：
![image](http://upload-images.jianshu.io/upload_images/1522494-eff2978a19553ec5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)