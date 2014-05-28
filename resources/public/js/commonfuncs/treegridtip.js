/**
 * Created by jack on 14-1-26.
 */

/**
 * Datagrid扩展方法tooltip 基于Easyui 1.3.3，可用于Easyui1.3.3+
 * 简单实现，如需高级功能，可以自由修改
 * 使用说明:
 *   在easyui.min.js之后导入本js
 *   代码案例:
 *		$("#dg").datagrid({....}).datagrid('tooltip'); 所有列
 *		$("#dg").datagrid({....}).datagrid('tooltip',['productid','listprice']); 指定列
 * @author ____′↘夏悸
 */
$.extend($.fn.treegrid.methods, {
    tooltip : function (jq, fields) {


        return jq.each(function () {
            var panel = $(this).treegrid('getPanel');

            if (fields && typeof fields == 'object' && fields.sort) {
                $.each(fields, function (index) {
                    var field = this;
                    bindEvent($('.datagrid-body td[field=' + field + '] .datagrid-cell', panel));
                });
            } else {
                bindEvent($(".datagrid-body .datagrid-cell", panel));
            }
        });

        function bindEvent(jqs) {
            jqs.mouseover(function () {
                var content = $(this).find('.treedivision').attr('signaturepath');
                if(content==null||content==='null'||content==='') content="无签章图片";
                else content='<img width="200" height="200" src="'+content+'">';
                $(this).tooltip({
                    content : content,
                    trackMouse : true,
                    onHide : function () {
                        $(this).tooltip('destroy');
                    }
                }).tooltip('show');
            });
        }
    }
});

