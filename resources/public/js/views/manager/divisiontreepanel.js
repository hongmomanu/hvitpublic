define(function () {

    function render(parameters) {

        $('#divisionmanagerpanel').treegrid({
            rownumbers: true,
            method: 'post',
            url: '../auth/gettreedivision',
            treeField: 'text',
            idField: 'id',
            onBeforeLoad: function (row, params) {
                if (!row)params.node = -1;
                else params.node = row.id;

            },
            onLoadSuccess: function (row, data) {
                require(['commonfuncs/treegridtip'], function () {
                    $("#divisionmanagerpanel").treegrid('tooltip', ['text']);
                });

            },
            onClickRow: function (rowData) {
                //console.log(rowData);
                rowData.divisionname = rowData.textold;
                rowData.divisionid = rowData.id;
                rowData.divisionpath = rowData.divisionpath.slice(0,rowData.divisionpath.lastIndexOf(rowData.textold));
                $('#divisioninfoform').form('load', rowData);
                $('#divisionformbtns .save,#divisionformbtns .del').linkbutton('enable');
                $('#divisionmanagerlayout').layout('expand', 'east');
            }

        });

        $('#divisionformbtns .del').click(function () {
            $.messager.confirm('确定要删除行政区划么?', '你正在试图删除行政区划?', function (r) {
                if (r) {
                    require(['jqueryplugin/easyui-form', 'commonfuncs/AjaxForm']
                        , function (easyuifrom, ajaxfrom) {
                            var params = $('#divisioninfoform').form("serialize");
                            var success = function () {
                                $.messager.alert('操作成功', '删除行政区划成功!');
                                if (params.parentid == -1)$('#divisionmanagerpanel').treegrid('reload');
                                else $('#divisionmanagerpanel').treegrid('reload', params.parentid);
                            };
                            var errorfunc = function () {
                                $.messager.alert('操作失败', '删除行政区划失败!');
                            }
                            ajaxfrom.ajaxsend('post', 'json', '../auth/deldivision', params, success, null, errorfunc)

                        });
                }
            });
        });
        $('#divisionformbtns .save').click(function () {
            $.messager.confirm('确定要修改角色配置么?', '你正在试图角色配置?', function (r) {
                    if (r) {
                        require(['jqueryplugin/easyui-form', 'commonfuncs/AjaxForm']
                            , function (easyform, ajaxfrom) {
                                var params = $('#divisioninfoform').form("serialize");
                                params.divisionpath=params.divisionpath+params.divisionname;
                                var success = function (res) {

                                    if(res.success){
                                        $.messager.alert('操作成功', '修改行政区划成功!');
                                        if(params.parentid==-1){
                                            $('#divisionmanagerpanel').treegrid('reload');
                                        }
                                        else{
                                            $('#divisionmanagerpanel').treegrid('reload',params.parentid);
                                        }

                                    }else{
                                        $.messager.alert('操作失败', res.msg);
                                    }
                                };
                                var errorfunc = function () {
                                    $.messager.alert('操作失败', '修改行政区划失败!');
                                };
                                ajaxfrom.ajaxsend('post', 'json', '../auth/editdivision', params, success, null, errorfunc);
                            });
                    }
                }
            );

        });

        $('#divisionformbtns .new').click(function () {

                require(['jqueryplugin/easyui-form', 'commonfuncs/AjaxForm']
                    , function (easyform, ajaxfrom) {
                        var params = $('#divisioninfoform').form("serialize");
                        //params.parentid = params.divisionid;
                        var success = function (res) {
                            if(res.success){
                                $.messager.alert('操作成功', '新增行政区划成功!');
                                $('#divisionmanagerpanel').treegrid('reload', params.parentid);
                            }else{
                                $.messager.alert('操作失败', res.msg);
                            }



                        };
                        var errorfunc = function () {
                            $.messager.alert('操作失败', '新增行政区划失败!');
                        };
                        ajaxfrom.ajaxsend('post', 'json', '../auth/adddivision', params, success, null, errorfunc);
                    });
            }
        );


        $('#divisionpaneltb .newdivision').click(function () {
            if ($('#newdivisionwin').length > 0) {
                $('#newdivisionwin').dialog('open');
            } else {
                require(['text!views/manager/newdivisionwin.htm', 'views/manager/newdivisionwin'],
                    function (div, newdivisionjs) {
                        $('body').append(div);
                        newdivisionjs.render();
                    });
            }

        });
        $('#divisioninfoform .resetchapter').click(function () {
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