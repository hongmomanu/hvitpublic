define(function () {

    function render(parameters) {
        $('#edituserpasswin').dialog({
            title: '修改密码窗口',
            width: 300,
            height: 180,
            closed: false,
            cache: false,
            buttons:[{
                text:'保存',
                id:'savenewpasswordbtn',
                handler:function(){
                   //alert(1);
                    require(['commonfuncs/md5','jqueryplugin/easyui-form','commonfuncs/AjaxForm']
                        ,function(md5,easyform,ajaxfrom){


                            var params=$('#edituserpasswin form').form("serialize");
                            params.password=CryptoJS.enc.Base64.stringify(CryptoJS.MD5(params.password));
                            params.oldpassword=CryptoJS.enc.Base64.stringify(CryptoJS.MD5(params.oldpassword));
                            params.username=username;
                            params.displayname=displayname;
                            params.iscommon=true;
                            params.userid=userid;
                            var success=function(res){
                                if(res.success){
                                    $.messager.alert('操作成功','修改密码成功!');
                                    $('#edituserpasswin').dialog('close');
                                }else{
                                    $.messager.alert('操作失败','原始密码错误!');
                                }

                            };
                            var errorfunc=function(){
                                $.messager.alert('操作失败','原始密码错误!');
                            }
                            ajaxfrom.ajaxsend('post','json','ajax/edituser.jsp',params,success,null,errorfunc)

                        });

                }
            },{
                text:'取消',
                handler:function(){
                    $('#edituserpasswin').dialog('close');
                }
            }],
            //href: 'get_content.php',
            modal: true
        });


    }

    return {
        render: render

    };
});