
/**********************language Switch function ********************************/
// $("#lang_toggle").click(function(){
//     $("body").toggleClass("ar").trigger('classChange');
// });


/**********************language Switch function ********************************/
$("#lang_toggle").click(function(){

   //$("body").toggleClass("ar").trigger('classChange');

 

   if ($("body").hasClass("ar")) {

        $(".logo_lang").attr("src", "assets/images/arabic-logo.svg");


 } else {

  $(".logo_lang").attr("src", "assets/images/eng-logo.svg");

}

 

});   

/**********************language Switch function close********************************/



/**************************************Contrast Js*******************************************************/
$(document).ready(function(){

   var highContrastFlag = Utils.getCookie('contrast');

   if(highContrastFlag=="true"){

    Utils.addHighContrastCSS();

   }

   $('#themechange').click(function(){

    ($("body").toggleClass("contrastar"));

     highContrastFlag = Utils.getCookie('contrast');

    if(highContrastFlag!="true"){

      Utils.addHighContrastCSS();

      $(".head_logo").attr("src", "assets/images/h-logo.svg");

      var contrastCookie = {'contrast' : 'true'}

      Utils.setCookie(contrastCookie, '5', '/');

    } else {

      Utils.removeHighContrastCSS();

      var contrastCookie = {'contrast' : 'false'}

      Utils.setCookie(contrastCookie, '5', '/');

    }

    });

  

   

   $("#themechange").click(function(){

      if ($("body").hasClass("ar","contrastar")) {

           $(".logo_lang").attr("src", "assets/images/h-white-logo.svg");

   

    }

    else if($("body").hasClass("contrastar")) {

     $(".logo_lang").attr("src", "assets/images/h-logo.svg");

    }

  

});

});
   
   /**************************************Contrast Js close*******************************************************/


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


//22-10-2020 hayagreeva changes for stop scrolling  on click anchor move on top 
// $("a").on('click', function(event) {
//    event.preventDefault();
//  });
 
// end here




/********************Search Select Persona and  its persona value Js******************************/
    $(".select_persona").click(function(e){
        e.stopPropagation();     
        $(".persona_tag_wrapper").toggleClass("active");
    });
   //  $("body").click(function(){
   //      $(".persona_tag_wrapper").removeClass("active");
   //  });

     $(document).on("click", ".select_persona", function(){
        $(".persona_tag_wrapper").addClass("active");
    });




    
    $("input[type='radio']").click(function(){
         var radioValue = $("input[name='persona']:checked").val();
         if(radioValue){
            $("#persona").val(radioValue)
         }
   });

   $(".tool_tip").click(function(e){
      e.stopPropagation()
   })

/********************Search Select Persona  its persona value Js Close******************************/


/*********************Mobile Menu Toggle***********************************************************/
$("#mob_menu_toggle").click(function(){
    $("#head1_top").toggleClass("head1_top");
    $("#head2_top").removeClass("head2_top");
    $(".directory_modal").removeClass("active");
    $(".media_modal").removeClass("active");
    $(".about_submenu").removeClass("active");
    $("body").removeClass("show_overlay");
    $("#expore_services_container").removeClass("view_exp_services");
});

$("#mob_menu_toggle2").click(function(){
   $("#head2_top").toggleClass("head2_top");
   $("#head1_top").removeClass("head1_top");
   $(".directory_modal").removeClass("active");
   $(".media_modal").removeClass("active");
   $(".about_submenu").removeClass("active");
   $("body").removeClass("show_overlay");
   $("#expore_services_container").removeClass("view_exp_services");
});


$(".mob_hide .link").click(function(e){
   e.stopPropagation();
   e.preventDefault();
   $(".directory_modal").removeClass("active");
   $(".media_modal").removeClass("active");
   $(".about_submenu").removeClass("active");
   $("body").removeClass("show_overlay");
   $("#expore_services_container").removeClass("view_exp_services");
   $(".access_submenu").toggleClass("active")
   $("body").addClass("show_overlay")

   // if($("access_submen").hasClass("active")){
   //       $("body").find(".access_submen.active").hide();
   // }

});

// $("body").click(function(e){
//    e.preventDefault();
//    $(".access_submenu").removeClass("active")
// });


// $(".nav_menu ul li a").click(function(){
//    $("#head2_top").removeClass("head2_top");
  
// });

$(".abt_qtr_nav ul li a").click(function(){
   $("#head1_top").removeClass("head1_top");
});


$(".about_qatar_nav").click(function(e){
   e.stopPropagation()
   $(".about_submenu").addClass("active");
   $(".directory_modal").removeClass("active");
   $(".media_modal").removeClass("active");
   $("#expore_services_container").removeClass("view_exp_services");
   $("body").addClass("show_overlay")
});

// $("body").click(function(){
//    $(".overlay").removeClass("show_overlay") 
//    $(".about_submenu").removeClass("active");
// });



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


  //******* 19-10-2020 hayagreeva changes for removing modal on close apply button start here
 $(".apply_filter").click(function (e) {
   e.preventDefault();
   $(".category_filter_wrapper_layer").removeClass("active");
   $("body").removeClass("hidden_scroll");
   // $(".apply_filter").attr("disabled", true); 
   $(".apply_filter_tag_wrapper ul li").css('display', 'inline-block');
});

$('#rest_val').click(function (e) {
   e.preventDefault();
   $('.selectpicker').prop('selectedIndex',0);
});
// end here



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

//******* 14-10-2020 hayagreeva changes adding active class in event-list--- ****
$('.tabwrapper li a').click(function() {
   $('.tabwrapper li a.active').removeClass("active");
   $(this).addClass("active");
});
// end here


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



$(document).on("click", ".close_map_img", function(){
   $(".map_view .f_row").removeClass("responsive_map_view");
});


$(document).on("click", ".view_on_map", function(){
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

 $(".category_bookmark").click(function (e) {
   e.stopPropagation();
   e.preventDefault();
    $(this).toggleClass("save");
 });





 /***********************************Accordian J******************************************/

 function reset_acc() {
      $('.ac-title').removeClass('acc-active');
      $('.accordian-para').slideUp();
      $('.plus-icon').removeClass('cross-icon');
   }
   $(document).on('click', '.ac-title', function (e) {
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
   
   function resetshare(){
      $(".share_kit_wrapper").removeClass("active");
      $(".share_g").removeClass("active");
   }
   
   $(".share_g").click(function(e){
       e.stopPropagation();
       e.preventDefault();
       resetshare()
      $(this).parent().siblings().addClass("active")
      //  $(".share_kit_wrapper").addClass("active");
       $(".share_g").removeClass("active");
       $(this).toggleClass("active");
    })


    $("body").click(function(){
      $(".share_kit_wrapper").removeClass("active");
      $(".share_g").removeClass("active");
   });



// 15-10-2020 hayagreeva changes for adding social icon in flag and amir page 
 $(".bottom_social .family-social").click(function(e){
   e.stopPropagation();
    $(".bottom_social .share_kit_wrapper").toggleClass("active");
});
// end here


   /*********************************Share Kit Icon Js Close*******************************************************/


 $(".txt_link").click(function () {
      $(".directory_card_row").slideToggle();
      $(".view_all").toggle();
      if ($(this).text() == "Collapse All")
         $(this).text("Expand All")
      else
         $(this).text("Collapse All");
});

/****************Articles & Service**********************/
$(document).ready(function(){
   $('window').on('load',  function(event) {
      event.preventDefault();
      $('#select_all').attr('checked', 'true');
    });
      $('#select_all').on('click',function(){
          if(this.checked){
              $('#select_all').each(function(){
                  this.checked = true;
              });
              $('.checkbox').each(function(){
                this.checked = false;
              });
  
          }
      });
     
      $('.checkbox').on('click',function(){
          if($('.checkbox:checked').length == $('.checkbox').length){
              $('#select_all').prop('checked',true);
          }else{
              $('#select_all').prop('checked',false);
          }
      });
  });
  $('.checkbox').change(function(event) {
    event.preventDefault();
    if($('.checkbox:checked').length == $('.checkbox').length){
      $('.checkbox').prop('checked',false);
    }
  });
  /****************Articles & Service Close**********************/   


/****************show more and less  more  fact**********************/
$(document).ready(function(){
$(".show_m").click(function(){

   $(".less_m").removeClass("hide");
   $(".show_m").addClass("hide");

      //  var text = $(".show_more_fact").text();
       
      //  if(text === "Less More Facts") {
      //    console.log(text)
      //      $(".show_more_fact").text('Show More Facts');
      //  } else {
      //      $(".show_more_fact").text('Less More Facts');
      //  }
   $(".fact_figure_card_wrap").addClass("active");  
//   $(".show_more").toggleClass("active");  
});


$(".less_m").click(function(){

   $(".less_m").addClass("hide");
   $(".show_m").removeClass("hide");
   $(".fact_figure_card_wrap").removeClass("active");   
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


   var selectedItem = $(this).val();
   // 15-10-2020 hayagreeva changes for adding option attribute on li tag
   var ftKey = $('option:selected', this).attr('ft-key');
   var ftValue = $('option:selected', this).attr('ft-value');


   $("li.custom_class").each(function() {
      if ($(this).hasClass(ftKey)){
      $(this).remove();
    }
    });
     $(".apply_filter_tag_wrapper ul").append('<li class="custom_class ' + ftKey + '" ft-key="' + ftKey + '" ft-value="' + ftValue + '">' + selectedItem + '<figure class="closetg" id=item'+ i +'><img src="/assets/images/remove-tag.png"  class="remove_tag" alt="remove-filter-tag"></figure></li>');
    i++;

});




$("body").on('click', 'li.custom_class', function(event) {
   event.preventDefault();
    var KeyVal =$(this).attr('ft-key');
  if(KeyVal != ''){
  
   $('select[ft-key="'+ KeyVal +'"]').prop('selectedIndex',0);
   $('select[ft-key="'+ KeyVal +'"]').trigger('change');
  }
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
















//    $(".polls_btn").click(function(){


//       $(this).hide();
//       $(this).parent().parent().parent().parent().parent().removeClass("polls_start");
//       $(this).parent().parent().parent().parent().parent().addClass("polls_inactive");

//       $("input[type='radio']:checked").parent().parent().addClass("active");

   
     
//    });

//    $("input[type='radio']").click(function(){
//       $(this).find(".category_polls_card ").removeClass("polls_start");
// })


  
/********Select Js***************/
$(function () {
  $('.selectpicker').selectpicker({});
  });

/********Select Js close***************/




/*************Uplaod File Path******************/
$('#upload_img').on('change', function () {
   var xVal = $(this).val();
   $('#attach_file').val(xVal);
});
/*************Uplaod File Path******************/







// $('.demo2').bootpag({
//    total: 23,
//    page: 3,
//    maxVisible: 10
// }).on('page', function(event, num){
//     $(".content2").html("Page " + num); // or some ajax content loading...
// });


// var a= $(".tags_wrap").attr('id');
// console.log(a)
// var  b=   $('#'+a).children().length;

// console.log(b);
// if(b>=2){
//    $('#'+a).append("<span class='tag_g tag_l' tabindex='0'>+2 more</span>")
// }




// $(document).ready(function(){
// var a= $(".tags_wrap").attr('id');
// console.log(a)
// var  b=   $('#'+a).children().length;

// console.log(b);
// if(b>=2){
//    $('#'+a).append("<span class='tag_g tag_l' tabindex='0'>+2 more</span>")
// }
// });


// for(i=1;i<=$(".category_card").length[0];i++){
//    console.log(i)
// }


// var a = $(".tags_wrap").attr('id');
// console.log(a)
// var  b =   $('#'+a).children().length;


// var  b=   $('#'+a ).children().length;
// console.log(b);
// if(b>=2){
//    $('#'+a ).append("<li class='tag_g  tag_l' tabindex='0'>+2 more</li>")
// }


// var i= $(".category_card  .tags_wrap").find('li').size();
// console.log(i)

// if(i>=2){
//    $('.tags_wrap' ).append("<li class='tag_g  tag_l' tabindex='0'>+2 more</li>")
// }




// var lengths = $(".tags_wrap").map(function(){
//    return $(this).find('li').length;
// }).get();


// console.log(lengths)

/**************************+2more category card*******************************************************/
$(".tags_wrap").map(function(){
      var d = $(this).find('li').length;
      console.log(d)
      if(d>3){
         $('li:nth-child(3)').nextAll('li').addClass("hide")
         $(this).append("<span class='tag_g  tag_l more-modal' tabindex='0' data-toggle='modal' data-target='#more-modal'>+ more</span>")
      }
})


$(document).on("click", ".more-modal", function(){
   e.preventDefault();
});



$(".insurance_mob").map(function(){
   var d = $(this).find('a').length;

   if(d>=2){
     $('a:nth-child(2)').nextAll('a').addClass("hide")
       $(this).append("<span class='more  more_data_modal' tabindex='0'>more</span>")
   }

   console.log(d)


   // if(d>=2){
   //  //  $('.insurance_mob').append("<span class='more' tabindex='0' data-toggle='modal' data-target='#more-modal'>more</span>")
   // }

})




$(".more_data_modal").click(function(){
   $(this).parent().next().addClass("active");
})
$(".close_card_modal").click(function(){
   $(".card_modal_wrapper").removeClass("active");
});



/**************************Like/Unlike Js***********************************************/
$(".like").click(function(){
   $(".like").toggleClass("active");
});
/******************************Like/Unlike Js Close***********************************/



/**********************************Header Menu Click*****************************************************/

function resetnav(){
   $(".nav_menu ul li a").removeClass("active");
}

// $(".nav_menu ul li a").click(function(){
//    resetnav();
//    $(this).addClass("active");
// })

$(".close_header_modal").click(function(){
   $("body").removeClass("show_overlay")   
   $("#media_wrapper").removeClass("active");
   $("#directory_wrapper").removeClass("active");
   $(".about_submenu").removeClass("active");
   $(".nav_menu ul li a").removeClass("active");
});

$(".close_header_modal").click(function(){
   $("body").removeClass("show_overlay");
   $("#expore_services_container").removeClass("view_exp_services");
});


$(".overlay, .explore_services_overlay").click(function(){
   console.log('explore');
   $("body").removeClass("show_overlay")   
   $("#media_wrapper").removeClass("active");
   $("#directory_wrapper").removeClass("active");
   $(".about_submenu").removeClass("active");
   $("#expore_services_container").removeClass("view_exp_services");
   $(".nav_menu ul li a").removeClass("active");
   $(".access_submenu").removeClass("active")
});



$(".view_dud").click(function(){
   $("body").addClass("show_overlay")   
   $(".directory_modal").addClass("active");
   $(".media_modal").removeClass("active");
   $(".about_submenu").removeClass("active");
   $("#expore_services_container").removeClass("view_exp_services");
   $(".access_submenu").removeClass("active")
});


$(".view_media").click(function(){
   $("body").addClass("show_overlay")   
   $(".directory_modal").removeClass("active");
   $(".media_modal").addClass("active");
   $(".about_submenu").removeClass("active");
   $("#expore_services_container").removeClass("view_exp_services");
   $(".access_submenu").removeClass("active")
});



$("#themechange").click(function(){
   $(".access_submenu ").removeClass("active")
   $("body").removeClass("show_overlay");  
});





$(".view_explore").click(function(){
   $('#expore_services_container').addClass('view_exp_services');
        $('.mob_nav').removeClass('active');
        $(".directory_modal").removeClass("active");
        $(".media_modal").removeClass("active");
        $("body").addClass("show_overlay");
        $(".about_submenu").removeClass("active"); 
        $(".access_submenu").removeClass("active")  
});





// $('.close_header_modal, .overlay').click(function(){
//    $("body").removeClass("show_overlay");
//    $("#services_inner").removeClass("show_explore_result")
// })





$(".directory_card_list").click(function(){
   $(".directory_list_card_wrap").removeClass("hide");
   $(".select_explore").addClass("hide");
});   

$(".back_directory").click(function(){
   $(".directory_list_card_wrap").addClass("hide");
   $(".select_explore").removeClass("hide");
});
/**********************************Header Menu Click close*****************************************************/






//28-10-2020 hayagreeva changes for showing time and day for embassies detail page
$(function() {
    var TodayDate = new Date().getDay();
    TodayDate = (TodayDate == 0) ? 7 : TodayDate;
    $('select[name=Todays_Day]').find('option').eq( TodayDate ).prop('selected', true)
    .end().change();
});


$(document).ready(function(){
    $("[name='Todays_Day']").change(function(){
        $(this).find("option:selected").each(function(){
            var optionValue = $(this).attr("value");
            if(optionValue){
                $(".time_container").not("." + optionValue).hide();
                $("." + optionValue).show();
            } else{
                $(".time_container").hide();
            }
        });
    }).change();
});





$(".login_user").click(function(e){
   e.stopPropagation();
   $(this).toggleClass("active");
   $(".sub_menu").toggleClass("active");
})


$("body").click(function(){
   $(".login_user").removeClass("active");
   $(".sub_menu").removeClass("active");
});


$(".view_on_map ").click(function(e){
 e.preventDefault()
 console.log("hua")
});

function reset_more(){
   $(".readmore_overlay ").removeClass("active")
}

$(document).on("click", ".know_more_p", function(e){
   e.preventDefault()
   reset_more();
   $(this).parent().next().addClass("active");
});

$(document).on("click", ".close_more_p", function(e){
   e.preventDefault();
   $(".readmore_overlay ").removeClass("active")
});






$(".face_bookmark").click(function(){
   $(this).toggleClass("active");
});



$('.video_img_wrapper .tab_wrapper a').click(function() {
     var href = $(this).attr('href');
       $('html,body').animate({
       scrollTop: $(href).offset().top - 150},
       '10');
   });


$('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
   var target = e.target.attributes.href.value;
   $(target +' .result_blocks li:first-child a').focus();
 });

 $(document).on("click", ".view_explore", function(){
   $('.serv_tabs ul li:first-child a').focus() //to set focus on explore services dialog
});

$(document).on("click", ".view_dud", function(){
   $('.category_tab_wrapper ul li:first-child a').focus()
});

$(document).on("click", '.view_media', function(){
   $('.category_tab_wrapper ul li:first-child a').focus()
});

// $(document).on("click", '.about_qatar_nav', function(){
//    $('.about_submenu .card_col:first-child a').focus()
// });

$(document).keydown(function(e) {
   // ESCAPE key pressed
   if (e.key == "Escape") {
      $("body").removeClass("show_overlay");
      $(".directory_modal").removeClass("active");
      $(".media_modal").removeClass("active");
      $("#expore_services_container").removeClass("view_exp_services");
      $(".about_submenu").removeClass("active");
      $('.view_explore').focus()
   }
});



$(".skip_navigation").click(function(e){
   if (e.key == "Escape"){
      $(this).addClass("focus")
   } 
})







//---------------------------Tab Focus -----------------------//

document.addEventListener('click', _handleMouseClick,true);
document.addEventListener('keydown',_keydown,true);
function _handleMouseClick(event){
    if(event.detail){
        document.getElementsByTagName("body")[0].classList.add("disableOutline");
    }
}
function _keydown(e){
    document.getElementsByTagName("body")[0].classList.remove("disableOutline");
}

//---------------------------Tab Focus -----------------------//