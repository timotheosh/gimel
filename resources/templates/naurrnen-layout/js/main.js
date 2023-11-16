/* Set the width of the side navigation to 250px and the left margin of the page content to 250px */
function openNav() {
  document.getElementById("side-nav").style.width = "480px";
}

/* Set the width of the side navigation to 0 and the left margin of the page content to 0 */
function closeNav() {
  document.getElementById("side-nav").style.width = "0";
}

/* Returns the root directory of a page. */
function getRootDir() {
  var pathSegments = window.location.pathname.split('/');
  if (pathSegments.length > 2) {
    return pathSegments[1];
  } else {
    return '';
  }
}


function collapsableContainers() {
  var coll = document.getElementsByClassName("collapsible");
  var i;

  for (i = 0; i < coll.length; i++) {
    coll[i].addEventListener("click", function () {
      this.classList.toggle("active");
      var content = this.nextElementSibling;
      if (content.style.maxHeight) {
        content.style.maxHeight = null;
      } else {
        content.style.maxHeight = content.scrollHeight + "px";
      }
    });
  }
}

function expandRootMenu() {
  // My dogfood
  var path = getRootDir();
  if (path.length > 0) {
    var element = document.getElementById(path);
    element.classList.toggle("active");
    element.nextElementSibling.style.display = "block";
  }
}

function toggleContainer() {
  // Code copy-pasted from w3schools
  var dropdown = document.getElementsByClassName("dropdown-btn");
  var i;
  for (i = 0; i < dropdown.length; i++) {
    dropdown[i].addEventListener("click", function () {
      this.classList.toggle("active");
      var dropdownContent = this.nextElementSibling;
      if (dropdownContent.style.display === "block") {
        dropdownContent.style.display = "none";
      } else {
        dropdownContent.style.display = "block";
      }
    });
  }
}
