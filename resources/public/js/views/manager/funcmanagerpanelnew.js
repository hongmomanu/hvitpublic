define(function () {

    function render(parameters) {
        $('#funcmanagerpanel').treegrid({
            rownumbers: true,
            method: 'post',

            url: '../auth/gettreefunc',
            treeField: 'text',
            idField: 'id',
            onBeforeLoad: function (row, params) {
                if (!row)params.node = -2;
                else params.node = row.id;
                params.roleid=$.getUrlParam('roleid');

            },
            onLoadSuccess: function (row, data) {
                /*require(['commonfuncs/treegridtip'], function () {
                    $("#funcmanagerpanel").treegrid('tooltip', ['text']);
                });*/

            },
            onClickRow: function (rowData) {
                rowData.funcname = rowData.textold;
                rowData.funcid = rowData.id;
                rowData.label = rowData.value;
                $('#funcinfoform').form('load', rowData);
                $('#funcformbtns .save,#funcformbtns .del').linkbutton('enable');
                $('#funcmanagerlayout').layout('expand', 'east');
            }

        });

        $('#funcformbtns .del').click(function () {
            $.messager.confirm('确定要删除功能么?', '你正在试图删除功能?', function (r) {
                if (r) {
                    require(['jqueryplugin/easyui-form', 'commonfuncs/AjaxForm']
                        , function (easyuifrom, ajaxfrom) {
                            var params = $('#funcinfoform').form("serialize");
                            var success = function () {
                                $.messager.alert('操作成功', '删除功能成功!');
                                if (params.pid == -1)$('#funcmanagerpanel').treegrid('reload');
                                else $('#funcmanagerpanel').treegrid('reload', params.pid);
                            };
                            var errorfunc = function () {
                                $.messager.alert('操作失败', '删除功能失败!');
                            }
                            ajaxfrom.ajaxsend('post', 'json', '../auth/delfunc', params, success, null, errorfunc)

                        });
                }
            });
        });
        $('#funcformbtns .save').click(function () {
            $.messager.confirm('确定要修改功能配置么?', '你正在试图功能配置?', function (r) {
                    if (r) {
                        require(['jqueryplugin/easyui-form', 'commonfuncs/AjaxForm']
                            , function (easyform, ajaxfrom) {
                                var params = $('#funcinfoform').form("serialize");
                                //testobj= $('#funcinfoform');
                                var success = function (res) {

                                    if(res.success){
                                        $.messager.alert('操作成功', '修改功能成功!');
                                        $('#funcmanagerpanel').treegrid('reload', params.pid);
                                    }else{
                                        $.messager.alert('操作失败', res.msg);
                                    }

                                };
                                var errorfunc = function () {
                                    $.messager.alert('操作失败', '修改功能失败!');
                                };

                                ajaxfrom.ajaxsend('post', 'json', '../auth/editfunc', params, success, null, errorfunc);
                            });
                    }
                }
            );

        });

        $('#funcformbtns .new').click(function () {

                require(['jqueryplugin/easyui-form', 'commonfuncs/AjaxForm']
                    , function (easyform, ajaxfrom) {
                        var params = $('#funcinfoform').form("serialize");
                        //params.parentid = params.funcid;
                        var success = function (res) {
                            if(res.success){
                                $.messager.alert('操作成功', '新增功能成功!');
                                if(params.pid<-1){
                                    $('#funcmanagerpanel').treegrid('reload') ;
                                }
                                else{
                                    $('#funcmanagerpanel').treegrid('reload',params.pid);
                                }

                            }else{
                                $.messager.alert('操作失败', res.msg);
                            }

                        };
                        var errorfunc = function () {
                            $.messager.alert('操作失败', '新增功能失败!');
                        };
                        ajaxfrom.ajaxsend('post', 'json', '../auth/addnewfunc', params, success, null, errorfunc);
                    });
            }
        );


        $('#funcpaneltb .newfunc').click(function () {
            if ($('#newfuncwin').length > 0) {
                $('#newfuncwin').dialog('open');
            } else {
                require(['text!views/manager/newfuncwin.htm', 'views/manager/newfuncwin'],
                    function (div, newfuncjs) {
                        $('body').append(div);
                        newfuncjs.render();
                    });
            }

        });
        $('#funcinfoform .resetchapter').click(function () {
            if ($('#newchapterwin').length > 0) {
                $('#newchapterwin').dialog('open');
            } else {
                require(['text!views/manager/newchapter.htm', 'views/manager/newchapter'],
                    function (div, newchapterjs) {
                        $('body').append(div);
                        newchapterjs.render();
                    });
            }

        });

    }

    return {
        render: render

    };
});