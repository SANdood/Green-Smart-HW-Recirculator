<h1>Green Smart HW Recirculator<h2>
SmartThings green automated HW recirculator

<h2>Background</h2>
Despite disagreements on whether they are actually green or not, whole-house hot water recirculation is becoming increasingly popular in new home buildings and retrofits. On the side of "green" is that you don't waste all that cold water running down the drain while you wait for the shower to warm up (to the tune of hundreds of gallons per year), while the anti- crowd complains that you heat water that is frequently not used.

Me, I just like the hotel idea of hot-water at the ready, all the time. Especially in the winter - I mean really - who wants to be standing around waiting for the shower to get warm?

In my attempt to split the difference, last year (pre-SmartThings), I found the Redy-Temp HW recirculator pump (http://www.redytemp.com/). This device offers several innovative and energy-saving features, including:

* Thermostatically-controlled return, with auto-shut off when return reaches a target temperature
* Momentary contact relay to signal HW demand, suitable for automation with X10 (then) or SmartThings (now)

I first installed using X10 motion sensors as described on the web site, but early in 2014 I got hooked on SmartThings and transitioned over to using a MIMOlite to signal HW demand, activated by a variety of ST actions, events and Hello, Home Activities. Initially, I used stock SmartApps (Light Turn on Motion, etc.) to automate the HW demand, but over time having 5 or 6 different Smart Apps associated with the HW demand switch got too complicated.

So I wrote this Smart App as a single place to control ALL aspects of HW demand that I can imagine, supporting both momentary contact initiators (like the RedyTemp) as well as always-on pumps controlled by a SmartThings switch, plug or outlet. I think I've captured pretty much every use case imaginable.

<h2>Use Cases</h2>
<ul>
<li><b>On with Motion:</b> Select as many motion sensors as you'd like. I use this one - if people are moving inside the house, the water will always be hot. Optionally stop demand when they stop moving.</li>
<li><b>On when Open:</b> When any specified door opens, turn on demand. The idea is opening the laundry room door means you will soon need hot water. Optionally stop the demand when the door is closed.</>
<li><b>On when Closed:</b> The inverse of the above - closing the bathroom door creates the demand (e.g., shower time). Optionally stop demand when the door is opened again.</li>
<li><b>On/Off with a Switch:</b> A virtual (or real) switch can signal demand - this can be used to link demand with things like IFTTT</b>
<li><b>Scheduled demand:</b> Turn on demand every XX minutes.</li>
<li><b>Location mode changes:</b> Turn on/off demand when the Hello, Home mode changes (e.g., On for "Home", Off when Away or Night)</li>
</ul>

What's cool is that you can use practically any combination of the above. In addition there are a few overrides that further optimize things:
* Stop demand after XX minutes
* Block ALL demand while location.mode is Away or Night (for me, this means "sleeping")
* Stop demand when Temperature Sensor sees a specified temp. For now, this one is experimental - I'm trying to use a SmartThings Multi-Sensor to mimic the thermostatic control of the Redy-Temp.
* Start demand when the Temperature Sensor falls below a specified temperature

With all these options, you can pretty much customize your house's recirculation pump recipe, no matter what kind of circulation pump you have (momentary contact or unswitched plug-in) - so long as you can control it with a ST-supported Z-wave or Zigbee switch, plug or outlet.

<h2>Caveats</h2>
Some things to note/consider:
* Using the Open/Closed contact sensors. For simplicity, you can select more than one door (contact sensor); if any of them opens, HW Demand is made; and if you enable the Off-on-Close, ANY of them closing will stop HW demand. With the RedyTemp, this isn't an issue (just don't enable Off-on-Close/Open).
* For many people, running the HW Demand longer isn't A Bad Thing - once the entire loop is heated, the HW heater will likely stop heating.
* That said, using Timed Off or a ST Thermometer will limit the time the pump runs, saving that electricity as well.
* If multiple doors are opened before the Timed Off event, they don't extend the Off Timer.
* In fact, every HW Demand call will check first to see if the Recirculator is already on, and if so it will not send another demand call. Note that if you are using a momentary-switched device like the RedyTemp setup I have, the switch will (almost always) immediately revert to Off() after turning On().

IMHO, (and FWIW), HW Recirclation works best when you have a complete loop in the HW piping. The alternative is to install these bypass valves that use the cold water pipes as the return. Unfortunately, this means that your cold water is always warm after HW Demand; you might minimize this by putting a thermometer on the cold water pipe under the sink at the most distant bypass valve (I personally have not tried this).

<h3>My Own Recipe</h3>
For those that are interested, here's how I use this app with my own RedyTemp pump (with dedicated return):
* Recirculation only while Home, not while Away or Night
  * Home starts when motion is detected in the Master Bath after 4:30AM; Night starts When Things Quiet Down
  * HW Demand requested on any change to "Home"
* HW Demand whenever motion detected anywhere inside the house
  * Motion sensors in hallways, kitchen, laundry room, etc.
* RedyTemp set to momentary demand (with a MIMOlite), and auto-stop when return water reaches 105F (source is 120F).

This works for me, but your own recipe can differ...try it, you might like it!

    Barry
