(function() {  //pllyfill for IE browser
    function remove() { this.parentNode && this.parentNode.removeChild(this); }
    if (!Element.prototype.remove) Element.prototype.remove = remove;
    if (Text && !Text.prototype.remove) Text.prototype.remove = remove;
  })();

$(document).ready(function(){
    // ========= show and hide explore section =======
    $("#view_explore").click(function(){
        $("#services_inner").addClass("show_explore_result")
    });

    $("#back_to_main").click(function(){
        $("#services_inner").removeClass("show_explore_result")
    })

    // ======= show clear search cross icon in sraech =======
    $('#myTab a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        $("#services_inner").removeClass("show_explore_result")
      });

    //   ======= view left filter on mobile =======
    $('.mobile_filter').click(function(){
        $('.explore_inner_content').addClass('show_mobile_filter');
        $('body, html').addClass('no_scroll')
    });
    $('.filter_backdrop').click(function(){
        $('.explore_inner_content').removeClass('show_mobile_filter');
        $('body, html').removeClass('no_scroll')
    });

    // ======= for lang change =======
    $('#lang_toggle').click(function(){
        $('body').toggleClass('ar')
    });
    $('.del_fav').click(function(){
        console.log("favourite")
        $('.hku_alert, .alert_backdrop').css("display", "block")
    });
// ======= to show alert when remove item from favourite =======
    $('.undo_button, .close_hku_alert').click(function(){
        console.log("favourite")
        $('.hku_alert, .alert_backdrop').css("display", "none")
    });

    // ======= to manage tutorial =======
    // localStorage.setItem("tutorial", 0);
    // viewTutorial = localStorage.getItem("tutorial");
    // console.log(viewTutorial);
    // if(viewTutorial == 0){
    //     $(".tutorial_backdrop").css("display", "block");
    //     $("#explore_tabs").css("z-index", "999")
    // }
})

// ======= show clear search cross icon in sraech =======
function clear_search(e){
    // console.log(e.value)
    if($(e).val() != ""){
        $(e).parent().find(".clear_search").css('display', 'block')
    }
    if($(e).val() == ""){
        $(e).parent().find(".clear_search").css('display', 'none')
    } 
}

// ======= clear text in search =======
function clear_text(e){
    $(e).parent().find(".inner_search_inp").val("");
    $(e).css('display', 'none')
}

var selected = [];
var applied_filters_arr = [];
function getSelValue(e){
    if($(e).is(':checked')){
        selected.push(e.value)
        var selectedSre = selected.toString();
        selectedSre = selectedSre.replace(/,/g, ", ");
        $(e).parents('.dropdown').find('input[type=text]').val(selectedSre);

        applied_filters_arr.push(e.value)
    }
    else{
        selected = $.grep(selected, function(value) {
            return value != e.value;
          });
        //   console.log(selected);
        $(e).parents('.dropdown').find('input[type=text]').val(selected);

        applied_filters_arr = $.grep(applied_filters_arr, function(value) {
            return value != e.value
        })
    }
    // console.log("filters", applied_filters_arr)
    applied_filters();
}

$('.dropdown').on('hidden.bs.dropdown', function () {
    selected = []
});

  function applied_filters(){
    var filter_tags = [];
    for(i = 0; i < applied_filters_arr.length; i++) {
        filter_tags.push("<li class='tag' title='"+applied_filters_arr[i]+"' onclick='remove_filter(this)'><p>"+applied_filters_arr[i]+"</p><span></span></li>")
    }
    $('.applied_option').html(filter_tags.join(""))
  }



  function remove_filter(e){
    applied_filters_arr = $.grep(applied_filters_arr, function(value) {
        return value != e.title
    });
    e.remove();
// console.log(applied_filters_arr)
    $('.check_input').prop('checked', false);
    var uncheck = document.getElementsByClassName('check_input');
    var thisTextbox;
    for (var i = 0; i < uncheck.length; i++){
        applied_filters_arr.map(function(v){
            if(uncheck[i].value === v){
                // console.log(uncheck[i])
                    uncheck[i].checked=true;

                    thisTextbox = uncheck[i];
                    // console.log(v, ":", selected)
                    // thisTextbox.parents('.dropdown').find('input[type=text]').value().replace(v, '');
                    selected = $.grep(selected, function(value) {
                        return value != v
                    });
                    console.log(v, ":", selected)
                }
            })
        }
    }

  function uncheckAll(e){
    $(e).parents('.dropdown-menu').find('.check_input').prop('checked', false);
    $(e).parents('.filter_col').find('input[type=text]').val('');

    var toRemoveAry = [];
    var chk = $(e).parents('.dropdown-menu').find('.check_input');
    for(i = 0; i<chk.length; i++){
        toRemoveAry.push(chk[i].value)
    }
    applied_filters_arr = applied_filters_arr.filter(function(el){
        return toRemoveAry.indexOf(el) < 0;
    });
    console.log(applied_filters_arr);
    applied_filters()
  }



  