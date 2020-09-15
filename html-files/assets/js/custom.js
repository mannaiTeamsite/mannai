
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
       console.log("A new date selection was made: " + start.format('DD-MM-YYYY') + ' to ' + end.format('DD-MM-YYYY'));
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
    $(".calendar_wrapper").removeClass("active");

 });

 $(".grid").click(function () {
    $(".list_row_wrapper").removeClass("list_view");
    $(".list_row_wrapper").removeClass("map_view");
    $(".map_col").removeClass("active");
    $(".calendar_wrapper").removeClass("active");
 });
 $(".map").click(function () {
    $(".list_row_wrapper").addClass("map_view");
    $(".list_row_wrapper").removeClass("list_view");
    $(".map_col").addClass("active");
    $(".calendar_wrapper").removeClass("active");
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





   /*********************************Share Kit Icon Js*******************************************************/
    $(".share_g").click(function(e){
       e.stopPropagation();
       $(".share_kit_wrapper").toggleClass("active");
       $(".share_g").removeClass("active");
       $(this).toggleClass("active");
    })


    $("body").click(function(){
      $(".share_kit_wrapper").removeClass("active");
      $(".share_g").removeClass("active");
   });
   /*********************************Share Kit Icon Js Close*******************************************************/





   $(".txt_link").click(function () {
      $(".directory_card_row").slideToggle();
      $(".view_all").toggle();
      if ($(this).text() == "Collapse All")
         $(this).text("Expand All")
      else
         $(this).text("Collapse All");
   });

/****************Articles**********************/
$(".article_card").click(function () {
   $(this).toggleClass("active");
   if (!$(this).hasClass('active')) {
      $(".all").removeClass("active");
   }
})
 
$(".all").click(function (e) {
 
   $(this).nextAll().toggleClass("active");
   if ($(this).hasClass('active')) {
      $(".article_card").addClass("active");
   }
})
/****************Articles Close**********************/




/****************show more and less  more  fact**********************/
$(document).ready(function(){
$(".show_more_fact").click(function(){

       var text = $(".show_more_fact").text();
       
       if(text === "Less More Facts") {
         console.log(text)
           $(".show_more_fact").text('Show More Facts-Watch');
       } else {
           $(".show_more_fact").text('Less More Facts');
       }
   $(".fact_figure_card_wrap").toggleClass("active");   
});

});

/****************show more and less  more  fact close**********************/


/**************************Pagenation Js***********************************************/
$(".pagination_wrapper li a").click(function(){
   $(".pagination_wrapper li a").removeClass("active")
   $(this).addClass("active");
});
/******************************Pagenation Js Close***********************************/




/******************************Filter JS**********************************************/
var i=1;
   
$('.selectpicker').change(function () {
   var selectedItem = $(this).val();
   $(".apply_filter_tag_wrapper ul").append('<li>' + selectedItem + '<figure class="closetg" id=item'+ i +'><img src="assets/images/remove-tag.png"  class="remove_tag" alt="remove-filter-tag"></figure></li>');
    i++;
});

$("body").on('click','.closetg',function(){
var $this = $(this);
var getId = $this.attr('id');
console.log(getId)
 // $(this).attr('data-in').parent().remove();
 $('#' + getId).parent().remove();
});


$('form').on('reset', function() {
setTimeout(function() {
$('.selectpicker').selectpicker('refresh');
});
});

$(".cancel").click(function(){

$(".apply_filter_tag_wrapper ul li").remove()
});
 
/******************************Filter JS Close**********************************************/







$(".calendar_view").click(function(){
   $(".calendar_wrapper").addClass("active");
   $(".list_row_wrapper").removeClass("map_view");
   $(".map_col").removeClass("active");
});






$(".links ul li a").click(function(){
   $(".links ul li a").removeClass("active")   
   
   $(this).addClass("active");
})




var myVideo = document.getElementById("video"); 

function playPause() { 
  if (myVideo.paused) {
    myVideo.play(); 
    $('.play img').attr('src', 'assets/images/pause.png');
    $(myVideo).parent().addClass('playing')
  }
  else 
  {
    myVideo.pause(); 
    $('.play img').attr('src', 'assets/images/play.png')
  }
} 