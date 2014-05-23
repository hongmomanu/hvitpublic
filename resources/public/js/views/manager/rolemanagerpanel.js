define(function () {

    function render(parameters) {
        $('#rolepaneltb .keyword').bind('click keypress',function(e){
            var keycode = (event.keyCode ? event.keyCode : event.which);
            if($(this).attr("type")==='keyword'&&keycode!=13)return;

                $('#rolemanagerpanel').datagrid('load',{keyword:$('#rolepaneltb .keyword').val()});
            }
        );

        $('#rolefuncgrid').datagrid({

            pagination:true,
            pageSize:10,
            onBeforeLoad: function (params) {
                var options = $('#rolefuncgrid').datagrid('options');
                params.start = (options.pageNumber - 1) * options.pageSize;
                params.limit = options.pageSize;
                params.totalname = "total";
                params.rowsname = "rows";
            },
            onLoadSuccess : function(data) {
                if (data) {
                    $.each(data.rows, function(index, item) {
                        if (item.selected) {
                            $('#rolefuncgrid').datagrid('selectRow',index);
                        }
                    });
                }
            }
        });

        $('#rolemanagerpanel').datagrid({
            singleSelect: true,
            collapsible: true,
            rownumbers: true,
            method:'post',
            url:'/auth/getroles',
            remoteSort: false,

            fit:true,
            toolbar:'#rolepaneltb',
            pagination:true,
            pageSize:10,
            onBeforeLoad: function (params) {
                var options = $('#rolemanagerpanel').datagrid('options');
                params.start = (options.pageNumber - 1) * options.pageSize;
                params.limit = options.pageSize;
                params.totalname = "total";
                params.rowsname = "rows";
            },
            onClickRow:function(index, rowData){
                $('#roleinfoform').form('load',rowData);
                $('#rolefuncgrid').datagrid('load', {
                    roleid: rowData.roleid
                });
                $('#roleformbtns .save,#roleformbtns .del').linkbutton('enable');
                $('#rolemanagerlayout').layout('expand','east');
            }

        });

        $('#roleformbtns .del').click(function(){
            $.messager.confirm('确定要删除角色配置么?', '你正在试图删除角色配置?', function(r){
                if (r){
                    require(['jqueryplugin/easyui-form','commonfuncs/AjaxForm']
                        ,function(easyuifrom,ajaxfrom){
                            var params=$('#roleinfoform').form("serialize");
                            var success=function(){
                                $.messager.alert('操作成功','删除角色成功!');
                                $('#rolemanagerpanel').datagrid('reload');
                            };
                            var errorfunc=function(){
                                $.messager.alert('操作失败','删除角色失败!');
                            }
                            ajaxfrom.ajaxsend('post','json','ajax/delrole.jsp',params,success,null,errorfunc)

                        });
                }
            });
        });
        $('#roleformbtns .save').click(function(){
            $.messager.confirm('确定要修改角色配置么?', '你正在试图角色配置?', function(r){
                    if (r){
                        require(['jqueryplugin/easyui-form','commonfuncs/AjaxForm']
                            ,function(easyform,ajaxfrom){


                               var formitem=$('#roleinfoform').form("serialize");

                                var selectItems=$('#rolefuncgrid').datagrid('getChecked');
                                var rows=$('#rolefuncgrid').datagrid('getRows');
                                var funcid_arr=[];
                                var delete_arr=[];
                                $.each(selectItems,function(index,item){
                                    funcid_arr.push(item.funcid);
                                });
                                $.each(rows,function(index,item){
                                    delete_arr.push(item.funcid);
                                });
                                var params = {
                                    roleid:formitem.roleid,
                                    deleteid:delete_arr,
                                    funcid:funcid_arr

                                };


                            var success=function(){
                                $.messager.alert('操作成功','配置角色功能成功!');
                                $('#rolemanagerpanel').datagrid('reload');
                            };
                            var errorfunc=function(){
                                $.messager.alert('操作失败','配置角色功能失败!');
                            };
                            ajaxfrom.ajaxsend('post','json','ajax/makerolefunc.jsp',params,success,null,errorfunc);
                        });
                    }
                }
            );

        });

        $('#rolepaneltb .newrole').click(function(){
            if($('#newrolewin').length>0){
                $('#newrolewin').dialog('open');
            }else{
                require(['text!views/manager/newrolewin.htm','views/manager/newrolewin'],
                    function(div,newrolejs){
                        $('body').append(div);
                        newrolejs.render();
                    });
            }

        });

    }

    return {
        render: render

    };
});