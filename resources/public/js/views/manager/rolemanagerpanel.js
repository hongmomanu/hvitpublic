define(function () {

    function render(parameters) {
        $('#rolepaneltb .keyword').bind('click keypress',function(e){
            var keycode = (event.keyCode ? event.keyCode : event.which);
            if($(this).attr("type")==='keyword'&&keycode!=13)return;

                $('#rolemanagerpanel').datagrid('load',{keyword:$('#rolepaneltb .keyword').val()});
            }
        );

        $('#rolefuncgrid').tree({

            method: 'post',
            animate:true,
            checkbox:true,
            url: '../auth/gettreefunc',
            treeField: 'text',
            idField: 'id',
            onBeforeLoad: function (row, params) {
                if (!row)params.node = -2;
                else params.node = row.id;
                try{
                    params.roleid=$('#rolemanagerpanel').datagrid('getSelected').id;
                }catch (err){
                }


            },
            onLoadSuccess: function (row, data) {
                /*require(['commonfuncs/treegridtip'], function () {
                 $("#funcmanagerpanel").treegrid('tooltip', ['text']);
                 });*/

            },
            onClickRow: function (rowData) {
                /*rowData.funcname = rowData.textold;
                rowData.funcid = rowData.id;
                rowData.label = rowData.value;
                $('#funcinfoform').form('load', rowData);
                $('#funcformbtns .save,#funcformbtns .del').linkbutton('enable');
                $('#funcmanagerlayout').layout('expand', 'east');*/
            }
        });

        $('#rolemanagerpanel').datagrid({
            singleSelect: true,
            collapsible: true,
            rownumbers: true,
            method:'post',
            url:'../auth/getroles',
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
                rowData.roleid=rowData.id;
                $('#roleinfoform').form('load',rowData);
                /*$('#rolefuncgrid').datagrid('load', {
                    roleid: rowData.roleid
                });*/
                $('#rolefuncgrid').tree('reload');
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
                            ajaxfrom.ajaxsend('post','json','../auth/delrole',params,success,null,errorfunc)

                        });
                }
            });
        });
        $('#roleformbtns .save').click(function(){
            $.messager.confirm('确定要修改角色配置么?', '你正在试图角色配置?', function(r){
                    if (r){
                        require(['jqueryplugin/easyui-form','commonfuncs/AjaxForm']
                            ,function(easyform,ajaxfrom){


                                var params=$('#roleinfoform').form("serialize");

                                var selectItems=$('#rolefuncgrid').tree('getChecked');
                                var unselectItems=$('#rolefuncgrid').tree('getChecked','unchecked');
                                var funcid_arr=[];
                                var delete_arr=[];
                                $.each(selectItems,function(index,item){
                                    funcid_arr.push(item.id);
                                });
                                $.each(unselectItems,function(index,item){
                                    delete_arr.push(item.id);
                                });
                                params.deleteid=$.toJSON(delete_arr);
                                params.funcid=$.toJSON(funcid_arr);



                            var success=function(){
                                $.messager.alert('操作成功','配置角色功能成功!');
                                $('#rolemanagerpanel').datagrid('reload');
                            };
                            var errorfunc=function(){
                                $.messager.alert('操作失败','配置角色功能失败!');
                            };
                            ajaxfrom.ajaxsend('post','json','../auth/makerolefunc',params,success,null,errorfunc);
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