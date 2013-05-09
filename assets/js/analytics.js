/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

	// cross-browser implementation of add event listener
	function addListener(element, type, expression)
	{
		if (window.addEventListener)
		{
			// W3C standard
			element.addEventListener(type, expression, false);
			return true;
		}
		else if (window.attachEvent)
		{
			// MS IE way
			element.attachEvent("on" + type, expression);
			return true;
		}
		else
		{
			return false;
		}
	}

	// cross-compatible GA tracker
	function trackEvent(descriptor)
	{
		try
		{
			// newer GA versions
			if (typeof pageTracker != "undefined")
			{
				pageTracker._trackPageview(descriptor);
			}
			// old urchin versions
			else if (typeof urchinTracker != "undefined")
			{
				urchinTracker(descriptor);
			}
		}
		catch (exception)
		{
			// alert('Exception: ' + exception);
		}
	}

	// parses any event fired
	function parseEvent(evt)
	{
		// cross-browser code to get the element related to the event
		var e = window.event ? evt.srcElement : evt.target;

		// the element is a traditional a-href link
		if (e.nodeName == "A")
		{
			// external links  - href should not point at the same hostname
			if (typeof e.href != "undefined" && e.href.indexOf(location.host) == -1)
			{
				// replace all non-alphanumeric characters and combine to a string
				var url = e.href.replace(/[^0-9|a-z|A-Z]/g, "_");
				var str = "/outgoing/-" + url;
				trackEvent(str);
			}

			// file downloads - href contains a known download file extension
			if (typeof e.href != "undefined" && e.href.match(/\.(doc|pdf|xls|ppt|zip|txt|vsd|vxd|js|css|rar|exe|wma|mov|avi|wmv|mp3)$/))
			{
				// replace all non-alphanumeric characters and combine to a string
				var url = e.href.replace(/[^0-9|a-z|A-Z]/g, "_");
				var str = "/download/-" + url;
				trackEvent(str);
			}
		}
		// track if it is an button event
		else if (e.type == "button" )
		{
				// replace all non-alphanumeric characters and combine to a string
				var name = e.name.replace(/[^0-9|a-z|A-Z]/g, "_");
				var value = e.value.replace(/[^0-9|a-z|A-Z]/g, "_");
				var str = "/event/-" + name + "-" + value;
				trackEvent(str);
		}
	}

	// add a single click listener to the document
	addListener(document, "click", parseEvent);
