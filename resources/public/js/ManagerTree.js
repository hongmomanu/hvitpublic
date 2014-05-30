/**
 * Created by jack on 13-12-31.
 */
define(function(){

    var a={

        render:function(parameters){
            $('#authorityacc,#systemgacc,#serverlogacc').tree({
                onClick: function (node){
                    var tree=$(this);
                    var me=this;
                    if(tree.tree('isLeaf', node.target)){
                        var isnew=false;
                        if($('#tabs').tabs('exists',1)){
                            $('#tabs').tabs('select',1);
                            isnew=$('#tabs').tabs('getSelected').panel('options').id!=node.id;
                        }
                        if(!$('#tabs').tabs('exists',1)||me.nodeid!=node.id||isnew){
                            var folder=tree.attr('folder');
                            var htmlfile='text!'+folder+node.value+'.htm';
                            var jsfile=folder+node.value;
                            var value=node.value;
                            var title=node.text;
                            require(['../js/TreeClickEvent.js'],function(TreeClickEvent){
                                TreeClickEvent.ShowContent(htmlfile,jsfile,title,value,folder,null,node.id);
                                me.nodeid=node.id;
                            });

                        }
                    }
                },
                onBeforeLoad:function(node, param){
                    param.leaf=true;
                    param.roleid=$.getUrlParam('roleid');
                    param.type=$(this).attr('name');
                },
                url:'../auth/getfuncsbyrole'

            });

        }
    }

    return a;
});