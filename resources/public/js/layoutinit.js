/**
 * Created by jack on 14-1-6.
 */
define(function(){



    function initroutnavigation(){

               $('#westpanel').panel({
                    onLoad:function(){
                        require(['../js/ManagerTree.js'],function(ManagerTree){
                            ManagerTree.render();
                        });
                    }

                });
                $('#westpanel').panel('refresh','../html/menu_qxgl.html');

    }

     return {
         initroutnavigation:initroutnavigation
     }

})