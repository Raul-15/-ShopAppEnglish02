package com.example.shopapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopapp.R
import com.example.shopapp.adapters.CartItemsListAdapter
import com.example.shopapp.firestore.FirestoreClass
import com.example.shopapp.models.Cart
import com.example.shopapp.models.Product
import com.example.shopapp.utils.Constants
import kotlinx.android.synthetic.main.activity_cart_list.*

class CartListActivity : BaseActivity() {
    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    private lateinit var mProductsList: ArrayList<Product>
    private lateinit var mCartListItems: ArrayList<Cart>


    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_cart_list)

        // TODO Step 3: Call the function to setup the action bar.
        // START
        setupActionBar()
        // END
        btn_checkout.setOnClickListener {
            val intent = Intent(this@CartListActivity, AddressListActivity::class.java)
            intent.putExtra(Constants.EXTRA_SELECT_ADDRESS, true)
            startActivity(intent)
        }
    }

    fun itemUpdateSuccess() {

        hideProgressDialog()

        getCartItemsList()
    }

    // TODO Step 8: Override the onResume function and call the function to getCartItemsList.
    // START
    override fun onResume() {
        super.onResume()

        // getCartItemsList()
        getProductList()
    }
    // END


    // TODO Step 2: Create a function to setup the action bar.
    // START
    /**
     * A function for actionBar Setup.
     */
    fun itemRemovedSuccess() {

        hideProgressDialog()

        Toast.makeText(
            this@CartListActivity,
            resources.getString(R.string.msg_item_removed_successfully),
            Toast.LENGTH_SHORT
        ).show()

        getCartItemsList()
    }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_cart_list_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_cart_list_activity.setNavigationOnClickListener { onBackPressed() }
    }
    // END

    // TODO Step 7: Create a function to get the list of cart items in the activity.
    // START
    private fun getCartItemsList() {

        // Show the progress dialog.
        // showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().getCartList(this@CartListActivity)
    }

    // END

    // TODO Step 5: Create a function to notify the success result of the cart items list from cloud firestore.
    // START
    /**
     * A function to notify the success result of the cart items list from cloud firestore.
     */
    fun successCartItemsList(cartList: ArrayList<Cart>) {

        // Hide progress dialog.
        hideProgressDialog()

        // TODO Step 2: Remove the for loop and display the list of cart items in the recycler view along with total amount.
        for (product in mProductsList) {
            for (cartItem in cartList) {
                if (product.product_id == cartItem.product_id) {

                    cartItem.stock_quantity = product.stock_quantity

                    if (product.stock_quantity.toInt() == 0) {
                        cartItem.cart_quantity = product.stock_quantity
                    }
                }
            }
        }
        mCartListItems = cartList

        if (cartList.size > 0) {

            rv_cart_items_list.visibility = View.VISIBLE
            ll_checkout.visibility = View.VISIBLE
            tv_no_cart_item_found.visibility = View.GONE

            rv_cart_items_list.layoutManager = LinearLayoutManager(this@CartListActivity)
            rv_cart_items_list.setHasFixedSize(true)

            val cartListAdapter = CartItemsListAdapter(this@CartListActivity, mCartListItems, true)
            rv_cart_items_list.adapter = cartListAdapter

            var subTotal: Double = 0.0

            for (item in cartList) {

                val price = item.price.toDouble()
                val quantity = item.cart_quantity.toInt()

                subTotal += (price * quantity)
            }

            tv_sub_total.text = "$$subTotal"
            // Here we have kept Shipping Charge is fixed as $10 but in your case it may cary. Also, it depends on the location and total amount.
            tv_shipping_charge.text = "$10.0"

            if (subTotal > 0) {
                ll_checkout.visibility = View.VISIBLE

                val total = subTotal + 10
                tv_total_amount.text = "$$total"
            } else {
                ll_checkout.visibility = View.GONE
            }

        } else {
            rv_cart_items_list.visibility = View.GONE
            ll_checkout.visibility = View.GONE
            tv_no_cart_item_found.visibility = View.VISIBLE
        }
    }

    fun successProductsListFromFireStore(productsList: ArrayList<Product>) {
        hideProgressDialog()
        // TODO Step 7: Initialize the product list global variable once we have the product list.
        // START
        mProductsList = productsList
        // END

        // TODO Step 8: Once we have the latest product list from cloud firestore get the cart items list from cloud firestore.
        // START
        getCartItemsList()

        // END
    }

    private fun getProductList() {

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().getAllProductsList(this@CartListActivity)
    }
    // END
}