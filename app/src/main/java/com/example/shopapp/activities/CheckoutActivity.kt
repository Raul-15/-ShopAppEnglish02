package com.example.shopapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopapp.R
import com.example.shopapp.adapters.CartItemsListAdapter
import com.example.shopapp.firestore.FirestoreClass
import com.example.shopapp.models.Address
import com.example.shopapp.models.Cart
import com.example.shopapp.models.Order
import com.example.shopapp.models.Product
import com.example.shopapp.utils.Constants


import kotlinx.android.synthetic.main.activity_cart_list.*
import kotlinx.android.synthetic.main.activity_checkout.*

class CheckoutActivity : BaseActivity() {

    // A global variable for the selected address details.
    private var mAddressDetails: Address? = null

    // TODO Step 7: Global variable for all product list.
    // START
    private lateinit var mProductsList: ArrayList<Product>
    // END

    // TODO Step 12: Global variable for cart items list.
    // START
    private lateinit var mCartItemsList: ArrayList<Cart>
    // END

    // START
    // A global variable for the SubTotal Amount.
    private var mSubTotal: Double = 0.0

    // A global variable for the Total Amount.
    private var mTotalAmount: Double = 0.0
    // END

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_checkout)

        setupActionBar()

        if (intent.hasExtra(Constants.EXTRA_SELECTED_ADDRESS)) {
            mAddressDetails =
                intent.getParcelableExtra<Address>(Constants.EXTRA_SELECTED_ADDRESS)!!
        }

        if (mAddressDetails != null) {
            tv_checkout_address_type.text = mAddressDetails?.type
            tv_checkout_full_name.text = mAddressDetails?.name
            tv_checkout_address.text = "${mAddressDetails!!.address}, ${mAddressDetails!!.zipCode}"
            tv_checkout_additional_note.text = mAddressDetails?.additionalNote

            if (mAddressDetails?.otherDetails!!.isNotEmpty()) {
                tv_checkout_other_details.text = mAddressDetails?.otherDetails
            }
            tv_checkout_mobile_number.text = mAddressDetails?.mobileNumber
        }
        // TODO Step 11: Assign a click event to the btn place order and call the function.
        // START
        btn_place_order.setOnClickListener {
            placeAnOrder()
        }
        // END

        // TODO Step 16: Call the function to get the product list.
        // START
        getProductList()
        // END
    }

    /**
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {

        setSupportActionBar(toolbar_checkout_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_checkout_activity.setNavigationOnClickListener { onBackPressed() }
    }

    // TODO Step 2: Create a function to get product list to compare it with the cart items stock.
    // START
    /**
     * A function to get product list to compare the current stock with the cart items.
     */
    private fun getProductList() {

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().getAllProductsList(this@CheckoutActivity)
    }
    // END

    // TODO Step 4: Create a function to get the success result of product list.
    // START
    /**
     * A function to get the success result of product list.
     *
     * @param productsList
     */
    fun successProductsListFromFireStore(productsList: ArrayList<Product>) {

        // TODO Step 8: Initialize the global variable of all product list.
        // START
        mProductsList = productsList
        // END

        // TODO Step 10: Call the function to get the latest cart items.
        // START
        getCartItemsList()
        // END
    }
    // END

    // TODO Step 9: Create a function to get the list of cart items in the activity.
    /**
     * A function to get the list of cart items in the activity.
     */
    private fun getCartItemsList() {

        FirestoreClass().getCartList(this@CheckoutActivity)
    }


    // TODO Step 11: Create a function to notify the success result of the cart items list from cloud firestore.
    // START
    /**
     * A function to notify the success result of the cart items list from cloud firestore.
     *
     * @param cartList
     */
    fun successCartItemsList(cartList: ArrayList<Cart>) {

        // Hide progress dialog.
        hideProgressDialog()

        for (product in mProductsList) {
            for (cart in cartList) {
                if (product.product_id == cart.product_id) {
                    cart.stock_quantity = product.stock_quantity
                }
            }
        }

        mCartItemsList = cartList

        rv_cart_list_items.layoutManager = LinearLayoutManager(this@CheckoutActivity)
        rv_cart_list_items.setHasFixedSize(true)

        val cartListAdapter = CartItemsListAdapter(this@CheckoutActivity, mCartItemsList, false)
        rv_cart_list_items.adapter = cartListAdapter

        // TODO Step 4: Replace the subTotal and totalAmount variables with the global variables.
        // START
        for (item in mCartItemsList) {

            val availableQuantity = item.stock_quantity.toInt()

            if (availableQuantity > 0) {
                val price = item.price.toDouble()
                val quantity = item.cart_quantity.toInt()

                mSubTotal += (price * quantity)
            }
        }

        tv_checkout_sub_total.text = "$$mSubTotal"
        // Here we have kept Shipping Charge is fixed as $10 but in your case it may cary. Also, it depends on the location and total amount.
        tv_checkout_shipping_charge.text = "$10.0"

        if (mSubTotal > 0) {
            ll_checkout_place_order.visibility = View.VISIBLE

            mTotalAmount = mSubTotal + 10.0
            tv_checkout_total_amount.text = "$$mTotalAmount"
        } else {
            ll_checkout_place_order.visibility = View.GONE
        }
        // END
    }
    // TODO Step 2: Create a function to prepare the Order details to place an order.
    // START
    /**
     * A function to prepare the Order details to place an order.
     */
    private fun placeAnOrder() {

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))

        // TODO Step 5: Now prepare the order details based on all the required details.
        // START
        val order = Order(
            FirestoreClass().getCurrentUserID(),
            mCartItemsList,
            mAddressDetails!!,
            "My order ${System.currentTimeMillis()}",
            mCartItemsList[0].image,
            mSubTotal.toString(),
            "10.0", // The Shipping Charge is fixed as $10 for now in our case.
            mTotalAmount.toString(),
        )
        // END

        // TODO Step 10: Call the function to place the order in the cloud firestore.
        // START
        FirestoreClass().placeOrder(this@CheckoutActivity, order)
        // END
    }
    // END


    // TODO Step 8: Create a function to notify the success result of the order placed.
    // START
    /**
     * A function to notify the success result of the order placed.
     */
    fun orderPlacedSuccess() {

        hideProgressDialog()

        Toast.makeText(this@CheckoutActivity, "Your order placed successfully.", Toast.LENGTH_SHORT)
            .show()

        val intent = Intent(this@CheckoutActivity, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}