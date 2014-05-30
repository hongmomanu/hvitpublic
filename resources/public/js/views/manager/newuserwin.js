define(function () {

    function render(parameters) {
        $('#newuserwin').dialog({
            title: '新增用户窗口',
            width: 300,
            height: 280,
            closed: false,
            cache: false,
            buttons:[{
                text:'保存',
                id:'savenewuserbtn',
                disabled:true,
                handler:function(){
                   //alert(1);
                    require(['jqueryplugin/easyui-form','commonfuncs/AjaxForm']
                        ,function(easyform,ajaxfrom){


                            var params=$('#newuserwin form').form("serialize");
                            params.iscommon=false;
                            var success=function(){
                                $.messager.alert('操作成功','新增用户成功!');
                                $('#newuserwin').dialog('close');
                                $('#usermanagerpanel').datagrid('reload');
                            };
                            var errorfunc=function(){
                                $.messager.alert('操作失败','新增用户失败!');
                            }
                            ajaxfrom.ajaxsend('post','json','../auth/addnewuser',params,success,null,errorfunc)

                        });

                }
            },{
                text:'取消',
                handler:function(){
                    $('#newuserwin').dialog('close');
                }
            }],
            //href: 'get_content.php',
            modal: true
        });

        $.extend($.fn.validatebox.defaults.rules, {
            equals: {
                validator: function(value,param){
                    return value == $(param[0]).val();
                },
                message: '密码不一致！'
            }
        });
        var divitiontree=$('#newuserwin .easyui-combotree');
        divitiontree.combotree({
            url:'../auth/gettreedivision?node=-1',
            method: 'get',
            textField:'textold',
            onLoadSuccess:function(){
                /*if(!this.firstloaded){
                    //alert(1);
                    console.log(divitiontree.combotree('find',-1))
                    //divitiontree.combotree('setValue', divisionpath);
                    //this.firstloaded=true;
                }*/
            },
            onBeforeExpand: function (node) {
                divitiontree.combotree("tree").tree("options").url
                    = "../auth/gettreedivision?onlychild=true&node=" + node.id;
            },
            onHidePanel: function () {
               /* divitiontree.combotree('setValue',
                    divitiontree.combotree('tree').tree('getSelected').divisionpath);*/
            }
        });


        $('#newuserwin .lazy-combobox').combobox({
            onShowPanel: function () {
                var url = '../auth/getroles?start=0&limit=100' ;
                $(this).combobox('reload', url);
            }

        });


        $.parser.parse($('#newuserwin'));

        $('#newuserwin input').on('change',function(){
            var form=$('#newuserwin form');
            if(form.form('validate')){
                $('#savenewuserbtn').linkbutton('enable');
            }
            else{
                $('#savenewuserbtn').linkbutton('disable');
            }
        });

        $('#newuserwin .easyui-combobox,#newuserwin .easyui-combotree').combobox({
            onHidePanel:function(){
                var form=$('#newuserwin form');
                if(form.form('validate')){
                    $('#savenewuserbtn').linkbutton('enable');
                }else{
                    $('#savenewuserbtn').linkbutton('disable');
                }
            }
        })

    }

    return {
        render: render

    };
});