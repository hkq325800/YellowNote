package com.kerchin.yellownote.model;

import android.content.Context;
import android.os.Handler;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.kerchin.yellownote.fragment.FolderFragment;
import com.kerchin.yellownote.fragment.NoteFragment;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.utilities.SystemHandler;
import com.kerchin.yellownote.utilities.Trace;

/**
 * Created by Administrator on 2015/9/26 0026.
 */
public class Folder {
    String objectId;
    String name;
    int contain;

    public Folder(String objectId, String name, int contain) {
        this.name = name;
        this.contain = contain;
        this.objectId = objectId;
    }

    public void dec(final int amount) {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Folder");
        query.getInBackground(objectId, new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                if (e == null) {
                    int num = avObject.getInt("folder_contain");
                    avObject.put("folder_contain", num - amount);
                    avObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e == null) {
                                FolderFragment.isChanged4folder = true;
                                contain -= amount;
                                //刷新note界面
                                //handler.sendEmptyMessage(handle4reset);
                                Trace.d("saveFolderNum-" + amount + "成功");
                            } else {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    public String getName() {
        return name;
    }

    public int getContain() {
        return contain;
    }

    public String getObjectId() {
        return objectId;
    }

    public void reName(final Context context, final String newName, final Handler handler
            , final byte handle4respond) throws AVException {
        boolean isTheSame = false;
        for (int i = 0; i < MyApplication.listFolder.size(); i++) {
            if (newName.equals(MyApplication.listFolder.get(i).getName())) {
                isTheSame = true;
                break;
            }
        }
        if (!isTheSame) {
            AVQuery<AVObject> query = new AVQuery<AVObject>("Folder");
            AVObject Folder = query.get(objectId);
            Folder.put("folder_name", newName);
            Folder.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
                        Trace.d("reNameFolder 成功");
                        //线下修改
                        name = newName;
                        Trace.show(context, "更名成功");
                        handler.sendEmptyMessage(handle4respond);
                        NoteFragment.isChanged4note = true;
                        //将所有folder下的note移至新folder下 线上修改
                        if (contain != 0) {
                            try {
                                for (int i = 0; i < MyApplication.listNote.size(); i++) {
                                    if (MyApplication.listNote.get(i).getFolderId().equals(objectId)) {
                                        MyApplication.listNote.get(i).move2folder(context, newName, objectId);
                                    }
                                }
                            } catch (AVException e1) {
                                e1.printStackTrace();
                            }
                        }
                    } else {
                        Trace.show(context, "重命名失败" + Trace.getErrorMsg(e));
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void delete(final Context context, final int position, final SystemHandler handler, final byte handle4respond) throws AVException {
        if (contain == 0) {
            AVQuery<AVObject> query = new AVQuery<AVObject>("Folder");
            AVObject Folder = query.get(objectId);
            //直接删除
            Folder.deleteInBackground(new DeleteCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
                        MyApplication.listFolder.remove(position);
                        Trace.show(context, "删除成功");
                        handler.sendEmptyMessage(handle4respond);
                    } else {
                        Trace.show(context, "操作失败,请检查网络");
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    //在listFolder中查找符合mNote名字的 赋值给thisFolder
    public static Folder search4folder(String folderName) {
        for (int i = 0; i < MyApplication.listFolder.size(); i++) {
            if (MyApplication.listFolder.get(i).getName().equals(folderName)) {
                return MyApplication.listFolder.get(i);
            }
        }
        return null;
    }
}
