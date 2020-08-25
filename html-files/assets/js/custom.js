
/**********************language Switch function ********************************/
$("#lang_toggle").click(function(){
    $("body").toggleClass("ar");
});

/**********************language Switch function close********************************/


/**********************Onscroll Header Fixed ********************************/
$(window).scroll(function () {
   var scroll = $(window).scrollTop();
   if (scroll >= 200) {
       $("header").addClass("fixedherder");
   
   } else {
       $("header").removeClass("fixedherder");
   
   }
});
/**********************Onscroll Header Fixed close********************************/


/********Select Js***************/
$(function () {
    $('.selectpicker').selectpicker({});
    });

/********Select Js close***************/


/********************Search Select Persona and  its persona value Js******************************/
    $(".select_persona").click(function(e){
        e.stopPropagation();     
        $(".persona_tag_wrapper").toggleClass("active");
    });
    $("body").click(function(){
        $(".persona_tag_wrapper").removeClass("active");
    });

    $(".select_persona").click(function(){
       console.log("asa")
      $(".persona_tag_wrapper").addClass("active");
    });

    
    $("input[type='radio']").click(function(){
         var radioValue = $("input[name='persona']:checked").val();
         if(radioValue){
            $("#persona").val(radioValue)
         }
      });

/********************Search Select Persona  its persona value Js Close******************************/


/*********************Mobile Menu Toggle***********************************************************/
$("#mob_menu_toggle").click(function(){
    $(".mob_nav").toggleClass("active");
});
/*********************Mobile Menu Toggle close***********************************************************/





/*************************Date Range Calendar*****************************************/
$(function () {
    $('input[name="daterange"]').daterangepicker({
       opens: 'left'
    }, function (start, end, label) {
       console.log("A new date selection was made: " + start.format('YYYY-MM-DD') + ' to ' + end.format('YYYY-MM-DD'));
    });
 });
 /*************************Date Range Calendar*****************************************/





 /**************************Category  Filter Modal Js*************************************************/
 $(".filter").click(function (e) {
    e.stopPropagation();
    $(".category_filter_wrapper_layer").addClass("active");
    $("body").addClass("hidden_scroll");
 });
 $(".filter_close, .cancel").click(function () {
    $(".category_filter_wrapper_layer").removeClass("active");
    $("body").removeClass("hidden_scroll");
 });
  /**************************Category Filter Modal Js*************************************************/




/**************************Category (List,grid,map,calendar) tab Js*************************************************/

function resetClass() {
    $(".category_view_menu_wrap li a").removeClass("active");
    $(".list_row_wrapper > div").removeClass("list_view")
 }

 $(".category_view_menu_wrap li a").click(function () {
    resetClass();
    $(this).addClass("active");
 });
/**************************Category (List,grid,map,calendar) tab Js close*************************************************/


 $(".list").click(function () {
    $(".list_row_wrapper").addClass("list_view");
    $(".list_row_wrapper").removeClass("map_view");
    $(".map_col").removeClass("active");
 });

 $(".grid").click(function () {
    $(".list_row_wrapper").removeClass("list_view");
    $(".list_row_wrapper").removeClass("map_view");
    $(".map_col").removeClass("active");
 });
 $(".map").click(function () {
    $(".list_row_wrapper").addClass("map_view");
    $(".list_row_wrapper").removeClass("list_view");
    $(".map_col").addClass("active");
 });





 $(".sort").click(function (e) {
    e.stopPropagation();
    $(".sort_wrapr").addClass("active");
    $(".category_sort_wrapper_layer").addClass("active");
    $("body").addClass("hidden_scroll");
 });
 $(".category_sort_wrapper_layer").click(function () {
    $(".sort_wrapr").removeClass("active");
    $(".category_sort_wrapper_layer").removeClass("active");
    $("body").removeClass("hidden_scroll");
 });
 $(".mob_tab").click(function (e) {
    e.stopPropagation();
    $(".mob_tab_wrapr").addClass("active");
    $(".mob_tab_wrapr_layer").addClass("active");
    $("body").addClass("hidden_scroll");
 });
 $(".mob_tab_wrapr_layer").click(function () {
    $(".mob_tab_wrapr").removeClass("active");
    $(this).removeClass("active");
    $("body").removeClass("hidden_scroll");
 });

 $(".category_bookmark").click(function () {
    $(this).toggleClass("save");
 });





 /***********************************Accordian J******************************************/

 function reset_acc() {
   $('.ac-title').removeClass('acc-active');
   $('.accordian-para').slideUp();
   $('.plus-icon').removeClass('cross-icon');
   }
   $('.ac-title').click(function (e) {
   e.preventDefault();
   if ($(this).hasClass('acc-active'))
   {
   reset_acc();
   }
   else {
   reset_acc();
   var getID = $(this).attr('data-in');
   $(getID).slideDown();
   $(this).addClass('acc-active');
   $(this).find('.plus-icon').addClass('cross-icon');
   }
   });

    /***********************************Accordian J******************************************/