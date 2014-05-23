define(function () {

    function render(parameters) {
        $('#enumpaneltb .keyword').bind('click keypress',function(e){
            var keycode = (event.keyCode ? event.keyCode : event.which);
            if($(this).attr("type")==='keyword'&&keycode!=13)return;

                $('#enummanagerpanel').datagrid('load',{keyword:$('#enumpaneltb .keyword').val()});
            }
        );
        $('#enummanagerpanel').datagrid({
            singleSelect: true,
            collapsible: true,
            rownumbers: true,
            method:'post',
            url:'ajax/getEnums.jsp',
            remoteSort: false,

            fit:true,
            toolbar:'#enumpaneltb',
            pagination:true,
            pageSize:10,
            onBeforeLoad: function (params) {
                var options = $('#enummanagerpanel').datagrid('options');
                params.start = (options.pageNumber - 1) * options.pageSize;
                params.limit = options.pageSize;
                params.totalname = "total";
                params.rowsname = "rows";
            },
            onClickRow:function(index, rowData){
                $('#enuminfoform').form('load',rowData);
                $('#enumformbtns .save,#enumformbtns .del').linkbutton('enable');
                $('#enummanagerlayout').layout('expand','east');
            }

        });

        $('#enumformbtns .del').click(function(){
            $.messager.confirm('确定要删除功能么?', '你正在试图删除功能?', function(r){
                if (r){
                    require(['jqueryplugin/easyui-form','commonfuncs/AjaxForm']
                        ,function(easyuifrom,ajaxfrom){
                            var params=$('#enuminfoform').form("serialize");

                            params.id=params.enumid;
                            params.isrowid=true;
                            params.idname='rowid';
                            params.tablename='enumerate';

                            var success=function(){
                                $.messager.alert('操作成功','删除枚举成功!');
                                $('#enummanagerpanel').datagrid('reload');
                            };
                            var errorfunc=function(){
                                $.messager.alert('操作失败','删除枚举失败!');
                            }
                            ajaxfrom.ajaxsend('post','json','ajax/delcommonbyid.jsp',params,success,null,errorfunc)

                        });
                }
            });
        });
        $('#enumformbtns .save').click(function(){
            $.messager.confirm('确定要修改枚举么?', '你正在试图修改枚举?', function(r){
                    if (r){
                        require(['jqueryplugin/easyui-form','commonfuncs/AjaxForm']
                            ,function(easyform,ajaxfrom){


                            var params=$('#enuminfoform').form("serialize");


                            params.id=params.enumid;
                            params.isrowid=true;
                            params.idname='rowid';
                            params.tablename='enumerate';
                            delete  params.enumid;
                            var success=function(){
                                $.messager.alert('操作成功','修改枚举成功!');
                                $('#enummanagerpanel').datagrid('reload');
                            };
                            var errorfunc=function(){
                                $.messager.alert('操作失败','修改枚举失败!');
                            };
                            ajaxfrom.ajaxsend('post','json','ajax/updatecommonbyid.jsp',params,success,null,errorfunc);

                        });
                    }
                }
            );

        });

        $('#enumpaneltb .newenum').click(function(){
            if($('#newenumwin').length>0){
                $('#newenumwin').dialog('open');
            }else{
                require(['text!views/manager/newenumwin.htm','views/manager/newenumwin'],
                    function(div,newenumjs){
                        $('body').append(div);
                        newenumjs.render();
                    });
            }

        });

    }

    return {
        render: render

    };
});