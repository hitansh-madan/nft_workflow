// SPDX-License-Identifier: MIT
pragma solidity ^0.8.4;

import "@openzeppelin/contracts/token/ERC721/utils/ERC721Holder.sol";
import "@openzeppelin/contracts/token/ERC1155/utils/ERC1155Holder.sol";
import "@openzeppelin/contracts/security/ReentrancyGuard.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/token/ERC721/IERC721.sol";
import "@openzeppelin/contracts/token/ERC1155/IERC1155.sol";
import "@openzeppelin/contracts/utils/Counters.sol";

contract NFTMarket is ERC721Holder, ERC1155Holder, Ownable, ReentrancyGuard {

    using Counters for Counters.Counter;

    Counters.Counter private _listingIdERC721Counter;
    Counters.Counter private _listingIdERC1155Counter;

    mapping(uint256 => Listing721) private _listingsERC721;
    mapping(uint256 => Listing1155) private _listingsERC1155;

    event listingCreated(uint256 listingId);

    struct Listing721 {
        address tokenAddress;
        uint256 tokenId;
        address ownerAddress; // address of the ownerAddress
        uint256 price; // price of the token
    }

    struct Listing1155 {
        address tokenAddress;
        uint256 tokenId;
        address ownerAddress; // address of the ownerAddress
        uint256 price; // price of the token
        uint256 quantity;
    }

    function createERC721Listing(address tokenAddress, uint256 tokenId, address ownerAddress, uint256 price) external nonReentrant {
        _listingIdERC721Counter.increment();
        _listingsERC721[_listingIdERC721Counter.current()] = 
            Listing721 (tokenAddress,tokenId,ownerAddress,price);
        
        IERC721(tokenAddress).transferFrom(ownerAddress, address(this), tokenId);
        emit listingCreated(_listingIdERC721Counter.current());
    }

    function createERC1155Listing(address tokenAddress, uint256 tokenId, address ownerAddress, uint256 price, uint256 quantity) external nonReentrant {
        _listingIdERC1155Counter.increment();
        _listingsERC1155[_listingIdERC1155Counter.current()] = 
            Listing1155 (tokenAddress,tokenId,ownerAddress,price,quantity);
        
        IERC1155(tokenAddress).safeTransferFrom(ownerAddress, address(this), tokenId, quantity,bytes(''));

        emit listingCreated(_listingIdERC1155Counter.current());
    }

    function buyERC721(uint256 listingId) external payable nonReentrant {
        require(msg.value == _listingsERC721[listingId].price ,'1');
        Listing721 memory listing = _listingsERC721[listingId];
        require (listing.tokenAddress !=  address(0),'2');

        IERC721(listing.tokenAddress).transferFrom(address(this), msg.sender, listing.tokenId);
        _listingsERC721[listingId] = Listing721(address(0),0,address(0),0);
    }

    function buyERC1155(uint256 listingId, uint256 quantity) external payable nonReentrant {
        require(msg.value == _listingsERC1155[listingId].price * quantity,'3');
        require(quantity <= _listingsERC1155[listingId].quantity,'4');
        Listing1155 memory listing = _listingsERC1155[listingId];
        require (listing.tokenAddress !=  address(0),'5');

        IERC1155(listing.tokenAddress).safeTransferFrom(address(this), msg.sender, listing.tokenId,quantity,bytes(''));
        if(quantity < listing.quantity) 
            _listingsERC1155[listingId].quantity -= quantity;
        else
            _listingsERC1155[listingId] = Listing1155(address(0),0,address(0),0,0);
    }

    function cancelERC721Listing(uint256 listingId) external nonReentrant {
        Listing721 memory listing = _listingsERC721[listingId];
        require(listing.tokenAddress != address(0),'6');

        IERC721(listing.tokenAddress).transferFrom(address(this), listing.ownerAddress, listing.tokenId);
        _listingsERC721[listingId] = Listing721(address(0),0,address(0),0);
    }

    function cancelERC1155Listing(uint256 listingId) external nonReentrant {
        Listing1155 memory listing = _listingsERC1155[listingId];
        require(listing.tokenAddress != address(0),'7');

        IERC1155(listing.tokenAddress).safeTransferFrom(address(this), listing.ownerAddress, listing.tokenId,listing.quantity,bytes(''));
        _listingsERC1155[listingId] = Listing1155(address(0),0,address(0),0,0);
    }
}