
/**********************language Switch function ********************************/
$("#lang_toggle").click(function(){
    $("body").toggleClass("ar").trigger('classChange');
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
    $("#head1_top").toggleClass("head1_top");
});

$("#mob_menu_toggle2").click(function(){
   $("#head2_top").toggleClass("head2_top");
});

$(".nav_menu ul li a").click(function(){
   $("#head2_top").removeClass("head2_top");
});

$(".abt_qtr_nav ul li a").click(function(){
   $("#head1_top").removeClass("head1_top");
});


$(".about_qatar_nav").click(function(e){
   e.stopPropagation()
   $(".about_submenu").toggleClass("active");
});

$("body").click(function(){
   $(".about_submenu").removeClass("active");
});


$(".reomve_about_menu").click(function(){
   $(".about_submenu").removeClass("active");
});

/*********************Mobile Menu Toggle close***********************************************************/

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





var c =$(".category_view_menu_wrap  li a").eq(0).html()

 if ( $('.category_view_menu_wrap li').length == 2 && $(".category_view_menu_wrap  li a").eq(1).html()=='List'){
    
   $(".category_view_menu_wrap").addClass("mobhide")
 }


/**************************Category (List,grid,map,calendar) tab Js close*************************************************/


 $(".list").click(function () {
    $(".list_row_wrapper").addClass("list_view");
    $(".list_row_wrapper").removeClass("map_view");
    $(".map_col").removeClass("active");
    $(".calendar_wrapper").removeClass("active");
    $(".map_list_button").removeClass("active");
    $(".category_search_input").removeClass("hide-element");
    $(".category_tab_wrapper").removeClass("hide-element");
    $(".categor_filter_wrap").removeClass("hide-element");
    $(".date_event").removeClass("show-element")

 });

 $(".grid").click(function () {
    $(".list_row_wrapper").removeClass("list_view");
    $(".list_row_wrapper").removeClass("map_view");
    $(".map_col").removeClass("active");
    $(".calendar_wrapper").removeClass("active");
    $(".map_list_button").removeClass("active");
    $(".category_search_input").removeClass("hide-element");
    $(".category_tab_wrapper").removeClass("hide-element");
    $(".categor_filter_wrap").removeClass("hide-element");
    $(".date_event").removeClass("show-element")
 });
 $(".map").click(function () {
    $(".list_row_wrapper").addClass("map_view");
    $(".list_row_wrapper").removeClass("list_view");
    $(".map_col").addClass("active");
    $(".calendar_wrapper").removeClass("active");
    $(".map_list_button").addClass("active");

   
    $(".category_search_input").removeClass("hide-element");
    $(".category_tab_wrapper").removeClass("hide-element");
    $(".categor_filter_wrap").removeClass("hide-element");
    $(".date_event").removeClass("show-element")



   });

   
$(".calendar_view").click(function(){
   $(".calendar_wrapper").addClass("active");
   $(".list_row_wrapper").removeClass("map_view");
   $(".map_col").removeClass("active");
   $(".map_list_button").removeClass("active");

   $(".category_search_input").addClass("hide-element");
   $(".category_tab_wrapper").addClass("hide-element");
   $(".categor_filter_wrap").addClass("hide-element");
   $(".date_event").addClass("show-element")
});



$(".map_list_button").click(function(){
   $(".map_view .f_row").toggleClass("responsive_map_view");
});

$(".close_map_img").click(function(){
   $(".map_view .f_row").removeClass("responsive_map_view");
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
      $(this).parent().siblings().toggleClass("active")

      //  $(".share_kit_wrapper").addClass("active");
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


/************************************Face Page Side Link js***************************************************/
$(".links ul li a").click(function(){
   $(".links ul li a").removeClass("active");   
   $(this).addClass("active");
});
/************************************Face Page Side Link js close***************************************************/


/************************************Face Page video play and pause js close***************************************************/
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
/************************************Face Page video play and pause js close***************************************************/


/**************************************readspeakar UI Js*******************************************************/
$(".lisitin").click(function(){
   $(".reader_ui").toggleClass("active")
});
/**************************************readspeakar UI Js close*******************************************************/


/**************************************Contrast Js*******************************************************/
$(document).ready(function(){
$('#themechange').click(function(){
if($('link#styles').attr('href')=="assets/css/Day.css"){
$('link#styles').attr('href','assets/css/blind.css');
$(".head_logo").attr("src", "assets/images/h-logo.png");
}
else
{
$('link#styles').attr('href','assets/css/Day.css');
 $(".head_logo").attr("src", "assets/images/logo.svg");
}
})
});
/**************************************Contrast Js close*******************************************************/


/**************************************Font plus and minus JS*******************************************/
var fontCount = 0;
$(document).ready(function(){
    $('.font_plus').click(function(){
        if(fontCount < 2){
            console.log(fontCount)
            font_plus();
            fontCount ++;
        }
        
    });
    $('.font_minus').click(function(){
        if(fontCount > 0 ){
            console.log(fontCount)
            font_minus()
            fontCount --;
        }
        
    });

})

function font_plus(){
    var a = document.querySelectorAll(" h1, h2, h3, h4, h5, h6, p, li, a, b, th, tr, td");
var fs;
    for(i = 0; i<a.length; i++){
        var fs = window.getComputedStyle(a[i]).fontSize.replace('px', '');
        fs ++;
        // if(a[i].style.fontSize){
            a[i].style.fontSize = fs + 'px' 
        // }
        
    }
}

function font_minus(){
    var a = document.querySelectorAll(" h1, h2, h3, h4, h5, h6, p, li, a, b, th, tr, td");
var fs;
    for(i = 0; i<a.length; i++){
        var fs = window.getComputedStyle(a[i]).fontSize.replace('px', '');
        fs --;
        a[i].style.fontSize = fs + 'px' 
    }
}
/**************************************Font plus and minus JS close*******************************************/





/*************************************Calendar js******************************************************************/
$(document).ready(function(){

  
   $('#demo').click();
   $(document).find('.today').parent().siblings().hide();
   $(document).find('td').css('pointer-events', 'none');
   $('.view_all').click(function(){
       console.log('click');
       if(!$(this).hasClass('min_view')){
           $(this).addClass('min_view');
           $(this).parent().addClass('full_view');
           $(document).find('.today').parent().siblings().show();
           $(document).find('td').css('pointer-events', 'auto');
       }
       else{
           $(this).removeClass('min_view');
           $(this).parent().removeClass('full_view');
           $(document).find('.today').parent().siblings().hide();
           $(document).find('td').css('pointer-events', 'none');
       }
   });
   
   
   
   });
   // ======= show current week only =======
   $(document).find('td').on('click', function(e){
      console.log('hello')
      e.preventDefault();
      e.stopPropagation();
   
   })
   // ======= show current week only =======
   //   ======= get current date =======
   var today = new Date();
   var dd = String(today.getDate()).slice(-2);
   var mm = String(today.getMonth() + 1).slice(-2); //January is 0!
   var yyyy = today.getFullYear();
   
   today = mm + '/' + dd + '/' + yyyy;
   //   ======= get current date =======
   if (window.matchMedia("(max-width: 600px)").matches) {
   $('#demo').daterangepicker({
   "parentEl": ".events",
   "singleDatePicker": true,
   "showDropdowns": true,
   "autoApply": true,
   "locale": {
       "format": "MM/DD/YYYY",
       "separator": " - ",
       "applyLabel": "Apply",
       "cancelLabel": "Cancel",
       "fromLabel": "From",
       "toLabel": "To",
       "customRangeLabel": "Custom",
       "weekLabel": "W",
       "daysOfWeek": [
           "Su",
           "Mo",
           "Tu",
           "We",
           "Th",
           "Fr",
           "Sa"
       ],
       "monthNames": [
           "January",
           "February",
           "March",
           "April",
           "May",
           "June",
           "July",
           "August",
           "September",
           "October",
           "November",
           "December"
       ],
       "firstDay": 1
   },
   "linkedCalendars": false,
   "showCustomRangeLabel": false,
   "alwaysShowCalendars": true,
   "startDate": today,
   //  "endDate": "09/16/2020",
   isInvalidDate: function(ele) {
     // console.log(ele);
   var currDate = moment(ele._d).format('YY-MM-DD');
   
   return ["20-09-09", "20-09-25", "20-09-20", "20-09-21"].indexOf(currDate) != -1;
   },
   isCustomDate: function(e){
   // console.log(e);
   var dataCell = moment(e._d).format("YYYY-MM-DD");
       if ( dataCell == '2020-09-05' || dataCell == '2020-09-15' || dataCell == '2020-09-01' || dataCell == '2020-09-12' ) {
          console.log(dataCell)
           return 'isEvent';
       }
   }
   }, function(start, end, label) {
   console.log('New date range selected: ' + start.format('YYYY-MM-DD') + ' to ' + end.format('YYYY-MM-DD') + ' (predefined range: ' + label + ')');
   });
   } else {
   $('#demo').daterangepicker({
   "parentEl": ".events",
   "singleDatePicker": true,
   "showDropdowns": true,
   "autoApply": true,
   "locale": {
       "format": "MM/DD/YYYY",
       "separator": " - ",
       "applyLabel": "Apply",
       "cancelLabel": "Cancel",
       "fromLabel": "From",
       "toLabel": "To",
       "customRangeLabel": "Custom",
       "weekLabel": "W",
       "daysOfWeek": [
           "Sunday",
           "Monday",
           "Tuesday",
           "Wednesday",
           "Thursday",
           "Friday",
           "Saturday"
       ],
       "monthNames": [
           "January",
           "February",
           "March",
           "April",
           "May",
           "June",
           "July",
           "August",
           "September",
           "October",
           "November",
           "December"
       ],
       "firstDay": 1
   },
   "linkedCalendars": false,
   "showCustomRangeLabel": false,
   "alwaysShowCalendars": true,
   "startDate": today,
   //  "endDate": "09/16/2020",
   isInvalidDate: function(ele) {
     // console.log(ele);
   var currDate = moment(ele._d).format('YY-MM-DD');
   
   return ["20-09-09", "20-09-25", "20-09-20", "20-09-21"].indexOf(currDate) != -1;
   },
   isCustomDate: function(e){
   // console.log(e);
   var dataCell = moment(e._d).format("YYYY-MM-DD");
       if ( dataCell == '2020-09-05' || dataCell == '2020-09-15' || dataCell == '2020-09-01' || dataCell == '2020-09-12' ) {
          console.log(dataCell)
           return 'isEvent';
       }
   }
   }, function(start, end, label) {
   console.log('New date range selected: ' + start.format('YYYY-MM-DD') + ' to ' + end.format('YYYY-MM-DD') + ' (predefined range: ' + label + ')');
   });
   }


   /*************************************Calendar js close******************************************************************/















   $(".polls_btn").click(function(){


      $(this).hide();
      $(this).parent().parent().parent().parent().parent().removeClass("polls_start");
      $(this).parent().parent().parent().parent().parent().addClass("polls_inactive");

      $("input[type='radio']:checked").parent().parent().addClass("active");

     
   });

   // function resetRadio(){
   //    $(".row_wrap").removeClass("active");   
   // }

   // $("input[type='radio']").click(function(){
   //   resetRadio()
   //   var b = $(this).attr('id');

   //    console.log(b);
      
   //    $('#'+ b ).parent().parent().addClass("active");

   // });
   /*************************Date Range Calendar*****************************************/
$(function () {
   $('input[name="daterange"]').daterangepicker({
      opens: 'left',
      format:'MM-YYYY',
   }, function (start, end, label) {
      console.log("A new date selection was made: " + start.format('MM-YYYY') + ' to ' + end.format('MM-YYYY'));
   });
});
/*************************Date Range Calendar*****************************************/
/********Select Js***************/
$(function () {
  $('.selectpicker').selectpicker({});
  });

/********Select Js close***************/









$('#pagination-here').bootpag({
   total: 10,          
   page: 1,            
   maxVisible: 5,     
   leaps: true,
   href: "#result-page-{{number}}",
})

//page click action
$('#pagination-here').on("page", function(event, num){
//show / hide content or pull via ajax etc
$("#content").html("Page " + num); 
});



var a= $(".tags_wrap ").attr('id');
console.log(a)
var  b=   $('#'+a).children().length;

console.log(b);
if(b>=2){
   $('#'+a).append("<span class='tag_g tag_l' tabindex='0'>+2 more</span>")
}



