define(function () {

    function render(parameters) {
        $('#funcpaneltb .keyword').bind('click keypress',function(e){
            var keycode = (event.keyCode ? event.keyCode : event.which);
            if($(this).attr("type")==='keyword'&&keycode!=13)return;

                $('#funcmanagerpanel').datagrid('load',{keyword:$('#funcpaneltb .keyword').val()});
            }
        );
        $('#funcmanagerpanel').datagrid({
            singleSelect: true,
            collapsible: true,
            rownumbers: true,
            method:'post',
            url:'ajax/getfuncs.jsp',
            remoteSort: false,

            fit:true,
            toolbar:'#funcpaneltb',
            pagination:true,
            pageSize:10,
            onBeforeLoad: function (params) {
                var options = $('#funcmanagerpanel').datagrid('options');
                params.start = (options.pageNumber - 1) * options.pageSize;
                params.limit = options.pageSize;
                params.totalname = "total";
                params.rowsname = "rows";
            },
            onClickRow:function(index, rowData){
                $('#funcinfoform').form('load',rowData);
                $('#funcformbtns .save,#funcformbtns .del').linkbutton('enable');
                $('#funcmanagerlayout').layout('expand','east');
            }

        });

        $('#funcformbtns .del').click(function(){
            $.messager.confirm('确定要删除功能么?', '你正在试图删除功能?', function(r){
                if (r){
                    require(['jqueryplugin/easyui-form','commonfuncs/AjaxForm']
                        ,function(easyuifrom,ajaxfrom){
                            var params=$('#funcinfoform').form("serialize");
                            var success=function(){
                                $.messager.alert('操作成功','删除功能成功!');
                                $('#funcmanagerpanel').datagrid('reload');
                            };
                            var errorfunc=function(){
                                $.messager.alert('操作失败','删除功能失败!');
                            }
                            ajaxfrom.ajaxsend('post','json','ajax/delfunc.jsp',params,success,null,errorfunc)

                        });
                }
            });
        });
        $('#funcformbtns .save').click(function(){
            $.messager.confirm('确定要修改功能么?', '你正在试图修改功能?', function(r){
                    if (r){
                        require(['jqueryplugin/easyui-form','commonfuncs/AjaxForm']
                            ,function(easyform,ajaxfrom){


                            var params=$('#funcinfoform').form("serialize");

                            var success=function(){
                                $.messager.alert('操作成功','修改功能成功!');
                                $('#funcmanagerpanel').datagrid('reload');
                            };
                            var errorfunc=function(){
                                $.messager.alert('操作失败','修改功能失败!');
                            };
                            ajaxfrom.ajaxsend('post','json','ajax/editfunc.jsp',params,success,null,errorfunc);

                        });
                    }
                }
            );

        });

        $('#funcpaneltb .newfunc').click(function(){
            if($('#newfuncwin').length>0){
                $('#newfuncwin').dialog('open');
            }else{
                require(['text!views/manager/newfuncwin.htm','views/manager/newfuncwin'],
                    function(div,newfuncjs){
                        $('body').append(div);
                        newfuncjs.render();
                    });
            }

        });

    }

    return {
        render: render

    };
});