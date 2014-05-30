define(function () {

    function render(parameters) {
        $('#logpaneltb .keyword').bind('click keypress',function(e){
            var keycode = (event.keyCode ? event.keyCode : event.which);
            if($(this).attr("type")==='keyword'&&keycode!=13)return;

                $('#logmanagerpanel').datagrid('load',
                    {
                        keyword:$('#logpaneltb .keyword').val(),
                        bgtime :$('#logpaneltb .bgtime').datebox('getValue'),
                        edtime :$('#logpaneltb .edtime').datebox('getValue')
                });
            }
        );



        $('#logmanagerpanel').datagrid({
            singleSelect: true,
            collapsible: true,
            rownumbers: true,
            method:'post',
            url:'../auth/getlogs',
            remoteSort: false,

            fit:true,
            toolbar:'#logpaneltb',
            pagination:true,
            pageSize:10,
            onBeforeLoad: function (params) {
                var options = $('#logmanagerpanel').datagrid('options');
                params.start = (options.pageNumber - 1) * options.pageSize;
                params.limit = options.pageSize;
                params.totalname = "total";
                params.rowsname = "rows";
            },
            onClickRow:function(index, rowData){
               /* $('#loginfoform').form('load',rowData);
                *//*$('#logfuncgrid').datagrid('load', {
                    logid: rowData.logid
                });*//*
                $('#logfuncgrid').tree('reload');
                $('#logformbtns .save,#logformbtns .del').linkbutton('enable');
                $('#logmanagerlayout').layout('expand','east');*/
            }

        });



    }

    return {
        render: render

    };
});